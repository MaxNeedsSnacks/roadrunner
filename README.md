<!-- TODO: Add Project icon 
![Project icon](https://git-assets.jellysquid.me/hotlink-ok/lithium/icon-rounded-128px.png)
-->

# Meep Meep!
![GitHub license](https://img.shields.io/github/license/MaxNeedsSnacks/meep-meep.svg)
![GitHub issues](https://img.shields.io/github/issues/MaxNeedsSnacks/meep-meep.svg)
![GitHub tag](https://img.shields.io/github/tag/MaxNeedsSnacks/meep-meep.svg)
[![This is a fork](https://img.shields.io/badge/This%20is%20a%20fork-Support%20the%20original%20mod!-fcb95b)](https://github.com/CaffeineMC/lithium-fabric/)
[![Architectury](https://img.shields.io/badge/built%20with-Architectury%20Loom-f95f1e)](https://github.com/architectury/architectury-loom)

Meep Meep! (also known as Road Runner) is an **unofficial fork** of the popular performance-enhancing Fabric mod [Lithium](https://github.com/CaffeineMC/lithium-fabric) by jellysquid3 for the Forge mod loader, based on the [Architectury Loom](https://github.com/architectury/architectury-loom) toolchain.
This mod aims to optimise many areas of the game in order to provide better overall performance for both Minecraft **clients and servers**, while **not requiring the mod to be installed on both sides**.

*(Note: Meep Meep!, as mentioned above is an unofficial fork and has thus not been endorsed by jellysquid3 or the CaffeineMC organisation. Please report any issues you have to **us only**, and we will forward them only if we are able to reproduce the problem on Fabric with the upstream version of Lithium!)*

### Downloads

You can currently find downloads for Meep Meep! through our [GitHub releases page](https://github.com/MaxNeedsSnacks/meep-meep/releases). A release on CurseForge is currently ~~not?~~ planned, but will take some time as we want to make sure this mod works **without any major issues** on Forge before pushing an initial build.

**That said, you have our express permission to include the GitHub releases builds of this mod in your CurseForge packs, and we will be asking to have those added to the approved non-CurseForge mods list, as well!**

### Installation instructions

Meep Meep! can be installed like any other Forge mod by dragging it into your modded Minecraft instance's `mods` folder.

### Issues and Contributing

If you'd like to get help with the mod, feel free to open an [issue](https://github.com/MaxNeedsSnacks/meep-meep/issues/) here on GitHub, and if you want to propose new features or otherwise contribute to the mod, we will gladly accept pull requests, as well!

### Support the (original) developers

Meep Meep! is only possible thanks to the many high-quality contributions made by the original Lithium developers, and as such, we would like to ask you to support *them*, instead! For more information, you can see [this section](https://github.com/CaffeineMC/lithium-fabric#support-the-developers) in the upstream repository's readme.

---

*(Note: The following paragraphs are taken almost verbatim from Lithium's README.md)*

### What makes this mod different?

One of the most important design goals in Meep Meep! is *correctness*. Unlike other mods which apply optimizations to the game, this mod does not sacrifice vanilla functionality or behavior in the name of raw speed. It's a no compromises' solution for those wanting to speed up their game, and as such, installing Meep Meep! should be completely transparent to the player.

If you do encounter an issue where we deviate from the norm (in this case both vanilla **and** default Forge behaviour), please don't hesitate to [submit an issue](https://github.com/MaxNeedsSnacks/meep-meep/issues/). Each patch is carefully checked to ensure vanilla parity, but after all, bugs are unavoidable.

### Configuration

Out of the box, no additional configuration is necessary once the mod has been installed. Meep Meep! makes use of a configuration override system which allows you to either forcefully disable problematic patches or enable incubating patches which are otherwise disabled by default. As such, an empty config file simply means you'd like to use the
default configuration, which includes all stable optimizations by default. 

For more detailed information on the different configuration file format and all available options, please see our [Wiki](https://github.com/MaxNeedsSnacks/roadrunner/wiki/Configuration-Files).

---

### Building from source

If you're hacking on the code or would like to compile a custom build of Meep Meep! from the latest sources, you'll want to start here.

#### Prerequisites

You will need to install JDK 8 in order to build this mod. You can either install this through a package manager such as
[Chocolatey](https://chocolatey.org/) on Windows or [SDKMAN!](https://sdkman.io/) on other platforms. If you'd prefer to
not use a package manager, you can always grab the installers or packages directly from
[AdoptOpenJDK](https://adoptopenjdk.net/).

On Windows, the Oracle JDK/JRE builds should be avoided where possible due to their poor quality. Always prefer using
the open-source builds from AdoptOpenJDK when possible.

#### Compiling

Navigate to the directory you've cloned this repository and launch a build with Gradle using `gradlew build` (Windows)
or `./gradlew build` (macOS/Linux). If you are not using the Gradle wrapper, simply replace `gradlew` with `gradle`
or the path to it.

The initial setup may take a few minutes. After Gradle has finished building everything, you can find the resulting
artifacts in `build/libs`.

---

### License

Meep Meep!, in accordance with Lithium's original license is licensed under GNU LGPLv3, a free and open-source license. For more information, please see the
[license file](https://github.com/MaxNeedsSnacks/meep-meep/blob/1.16.x/forge/LICENSE.txt).
