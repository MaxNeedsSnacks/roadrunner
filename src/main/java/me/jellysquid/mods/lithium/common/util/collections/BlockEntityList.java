package me.jellysquid.mods.lithium.common.util.collections;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("NullableProblems")
public class BlockEntityList implements List<BlockEntity> {
    //BlockEntityList does not support double-add of the same object. But it does support multiple at the same position.
    //This collection behaves like a set with insertion order. It also provides a position->blockEntity lookup.

    private final ReferenceLinkedOpenHashSet<BlockEntity> allBlockEntities;

    //When there is only 1 BlockEntity at a position, it is stored in posMap.
    //When there are multiple at a position, the first added is stored in posMap
    //and all of them are stored in posMapMulti using a List (in the order they were added)
    private final Long2ReferenceOpenHashMap<BlockEntity> posMap;
    private final Long2ReferenceOpenHashMap<List<BlockEntity>> posMapMulti;

    // Rough safety net for off-thread modifications: Goals in order of priority:
    //  1. No crashes on the "correct" thread. Crashes on wrong threads are acceptable
    //  2. Minimal performance impact server-side (where World already prevents off-thread access)
    //  3. Minimal performance impact for on-thread access client-side
    //  4. Minimal performance impact for off-thread access when no off-thread modifications have
    //  taken place "recently"
    // The idea is to record off-thread modifications in a thread-safe queue. On any on-thread
    // access the queue is added into the "main" state. If the queue is non-empty off-thread accesses
    // will create a temporary copy of the list and process the queue there (this should be a very
    // rare case in practice). This way the main thread never accesses the list while it is being
    // modified (since no other thread ever modifies it). The performance impact is a single
    // null-check per access server-side and a call to Thread.currentThread() and an atomic read
    // on the client
    @Nullable
    private final Thread ownerThread;
    // Queue of off-thread modifications. Only access this when synchronizing on it.
    private final List<OffThreadOperation> offThreadModifications = new ArrayList<>();
    // Used as a faster way of checking if offThreadModifications is empty. Using an enum instead
    // of a bool to easily handle processing of the off-thread modification queue
    private volatile ConcurrentState hasOffThreadModifications = ConcurrentState.CLEAN;

    private BlockEntityList(boolean hasPositionLookup, boolean hasOwnerThread) {
        this.posMap = hasPositionLookup ? new Long2ReferenceOpenHashMap<>() : null;
        this.posMapMulti = hasPositionLookup ? new Long2ReferenceOpenHashMap<>() : null;

        if (this.posMap != null) {
            this.posMap.defaultReturnValue(null);
            this.posMapMulti.defaultReturnValue(null);
        }

        this.ownerThread = hasOwnerThread ? Thread.currentThread() : null;
        this.allBlockEntities = new ReferenceLinkedOpenHashSet<>();
    }

    public BlockEntityList(List<BlockEntity> list, boolean hasPositionLookup, boolean hasOwnerThread) {
        this(hasPositionLookup, hasOwnerThread);
        this.addAll(list);
    }

    private BlockEntityList(BlockEntityList original) {
        this(original.posMap != null, true);
        if (original.posMap != null) {
            this.posMap.putAll(original.posMap);
            this.posMapMulti.putAll(original.posMapMulti);
        }
        this.allBlockEntities.addAll(original.allBlockEntities);

        synchronized (original.offThreadModifications) {
            this.hasOffThreadModifications = original.hasOffThreadModifications;
            this.offThreadModifications.addAll(original.offThreadModifications);
        }
    }

    @Override
    public int size() {
        if (!checkOffThreadModifications()) {
            return this.allBlockEntities.size();
        } else {
            return copyWithChanges().size();
        }
    }

    @Override
    public boolean isEmpty() {
        if (!checkOffThreadModifications()) {
            return this.allBlockEntities.isEmpty();
        } else {
            return copyWithChanges().isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        if (!checkOffThreadModifications()) {
            return this.allBlockEntities.contains(o);
        } else {
            return copyWithChanges().contains(o);
        }
    }

    @Override
    public Iterator<BlockEntity> iterator() {
        if (!checkOffThreadModifications()) {
            return this.allBlockEntities.iterator();
        } else {
            return copyWithChanges().iterator();
        }
    }

    @Override
    public Object[] toArray() {
        if (!checkOffThreadModifications()) {
            return this.allBlockEntities.toArray();
        } else {
            return copyWithChanges().toArray();
        }
    }

    @Override
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(T[] a) {
        if (!checkOffThreadModifications()) {
            return this.allBlockEntities.toArray(a);
        } else {
            return copyWithChanges().toArray(a);
        }
    }

    @Override
    public boolean add(BlockEntity blockEntity) {
        return this.addNoDoubleAdd(blockEntity, true);
    }

    private boolean addNoDoubleAdd(BlockEntity blockEntity, boolean exceptionOnDoubleAdd) {
        if (currentlyOffThread()) {
            // Off-thread adds are always treated as "if absent", otherwise the most typical case (race on
            // getBlockEntity) would always crash
            addOffThreadOperation(new OffThreadOperation(blockEntity, OperationType.ADD));
            return true;
        }
        checkOffThreadModifications();
        boolean added = this.allBlockEntities.add(blockEntity);
        if (!added && exceptionOnDoubleAdd
                //Ignore double add when we encounter vanilla's command block double add bug
                && !(blockEntity instanceof CommandBlockBlockEntity)) {
            this.throwException(blockEntity);
        }

        if (added && this.posMap != null) {
            long pos = getEntityPos(blockEntity);

            BlockEntity prev = this.posMap.putIfAbsent(pos, blockEntity);
            if (prev != null) {
                List<BlockEntity> multiEntry = this.posMapMulti.computeIfAbsent(pos, (long l) -> new ArrayList<>());
                if (multiEntry.size() == 0) {
                    //newly created multi entry: make sure it contains all elements
                    multiEntry.add(prev);
                }
                multiEntry.add(blockEntity);
            }
        }
        return added;
    }

    private void throwException(BlockEntity blockEntity) {
        throw new IllegalStateException("RoadRunner BlockEntityList" + (this.posMap != null ? " with posMap" : "") + ": Adding the same BlockEntity object twice: " + blockEntity.toTag(new CompoundTag()));
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity) o;
            if (currentlyOffThread()) {
                addOffThreadOperation(new OffThreadOperation(blockEntity, OperationType.REMOVE));
                return true;
            }
            checkOffThreadModifications();
            if (this.allBlockEntities.remove(o)) {
                if (this.posMap != null) {
                    long pos = getEntityPos(blockEntity);
                    List<BlockEntity> multiEntry = this.posMapMulti.get(pos);
                    if (multiEntry != null) {
                        multiEntry.remove(blockEntity);
                        if (multiEntry.size() <= 1) {
                            this.posMapMulti.remove(pos);
                        }
                    }
                    if (multiEntry != null && multiEntry.size() > 0) {
                        this.posMap.put(pos, multiEntry.get(0));
                    } else {
                        this.posMap.remove(pos);
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (!checkOffThreadModifications()) {
            return this.allBlockEntities.containsAll(c);
        } else {
            return copyWithChanges().containsAll(c);
        }
    }

    @Override
    public boolean addAll(Collection<? extends BlockEntity> c) {
        for (BlockEntity blockEntity : c) {
            this.add(blockEntity);
        }

        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends BlockEntity> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        for (Object obj : c) {
            modified |= this.remove(obj);
        }

        return modified;
    }

    @Override
    public void clear() {
        if (currentlyOffThread()) {
            // This should never happen in practice, if it does it should be easy to add support for it
            throw new UnsupportedOperationException();
        }
        checkOffThreadModifications();
        this.allBlockEntities.clear();
        if (this.posMap != null) {
            this.posMap.clear();
            this.posMapMulti.clear();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockEntity get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockEntity set(int index, BlockEntity element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, BlockEntity element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockEntity remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<BlockEntity> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<BlockEntity> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BlockEntity> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private static long getEntityPos(BlockEntity e) {
        return e.getPos().asLong();
    }


    public boolean addIfAbsent(BlockEntity blockEntity) {
        //we are not checking position equality but object/reference equality (like vanilla)
        //the hashset prevents double add of the same object
        return this.addNoDoubleAdd(blockEntity, false);
    }

    @SuppressWarnings("unused")
    public boolean hasPositionLookup() {
        return this.posMap != null;
    }

    //Methods only supported when posMap is present!
    public void markRemovedAndRemoveAllAtPosition(BlockPos blockPos) {
        if (currentlyOffThread()) {
            addOffThreadOperation(new OffThreadOperation(blockPos));
            return;
        }
        checkOffThreadModifications();
        long pos = blockPos.asLong();
        BlockEntity blockEntity = this.posMap.remove(pos);
        if (blockEntity != null) {
            List<BlockEntity> multiEntry = this.posMapMulti.remove(pos);
            if (multiEntry != null) {
                for (BlockEntity blockEntity1 : multiEntry) {
                    blockEntity1.markRemoved();
                    this.allBlockEntities.remove(blockEntity1);
                }
            } else {
                blockEntity.markRemoved();
                this.allBlockEntities.remove(blockEntity);
            }
        }
    }

    public BlockEntity getFirstNonRemovedBlockEntityAtPosition(long pos) {
        if (checkOffThreadModifications()) {
            return copyWithChanges().getFirstNonRemovedBlockEntityAtPosition(pos);
        }
        if (this.isEmpty()) {
            return null;
        }
        BlockEntity blockEntity = this.posMap.get(pos);
        //usual case: we find no BlockEntity or only one that also is not removed
        if (blockEntity == null || !blockEntity.isRemoved()) {
            return blockEntity;
        }
        //vanilla edge case: two BlockEntities at the same position
        //Look up in the posMultiMap to find the first non-removed BlockEntity
        List<BlockEntity> multiEntry = this.posMapMulti.get(pos);
        if (multiEntry != null) {
            for (BlockEntity blockEntity1 : multiEntry) {
                if (!blockEntity1.isRemoved()) {
                    return blockEntity1;
                }
            }
        }
        return null;
    }

    public boolean checkOffThreadModifications() {
        if (ownerThread == null) {
            // Off-thread modifications are not supported, so there aren't any to consider
            return false;
        }
        if (ownerThread != Thread.currentThread()) {
            // Currently off-thread
            return hasOffThreadModifications != ConcurrentState.CLEAN;
        }
        // Treat "CLEANING" as clean if on-thread to stop this method from recursing forever
        if (hasOffThreadModifications != ConcurrentState.DIRTY) {
            // There are no off-thread modifications
            return false;
        }
        synchronized (offThreadModifications) {
            hasOffThreadModifications = ConcurrentState.CLEANING;
            for (OffThreadOperation op : offThreadModifications) {
                switch (op.type) {
                    case ADD:
                        addIfAbsent(Preconditions.checkNotNull(op.blockEntity));
                        break;
                    case REMOVE:
                        remove(Preconditions.checkNotNull(op.blockEntity));
                        break;
                    case REMOVE_AT:
                        markRemovedAndRemoveAllAtPosition(Preconditions.checkNotNull(op.pos));
                        break;
                }
            }
            hasOffThreadModifications = ConcurrentState.CLEAN;
            offThreadModifications.clear();
        }

        // we just merged all off-thread modifications into the on-thread state
        return false;
    }

    private boolean currentlyOffThread() {
        return ownerThread != null && ownerThread != Thread.currentThread();
    }

    private void addOffThreadOperation(OffThreadOperation op) {
        synchronized (offThreadModifications) {
            offThreadModifications.add(op);
            hasOffThreadModifications = ConcurrentState.DIRTY;
        }
    }

    private BlockEntityList copyWithChanges() {
        BlockEntityList result = new BlockEntityList(this);
        Preconditions.checkState(!result.checkOffThreadModifications());
        return result;
    }

    private static class OffThreadOperation {
        private final BlockEntity blockEntity;
        private final BlockPos pos;
        private final OperationType type;

        private OffThreadOperation(BlockEntity blockEntity, OperationType type) {
            this.blockEntity = blockEntity;
            this.pos = null;
            this.type = type;
        }

        private OffThreadOperation(BlockPos at) {
            this.blockEntity = null;
            this.pos = at;
            this.type = OperationType.REMOVE_AT;
        }
    }

    private enum OperationType {
        ADD,
        REMOVE,
        REMOVE_AT
    }

    private enum ConcurrentState {
        CLEAN,
        DIRTY,
        CLEANING
    }
}
