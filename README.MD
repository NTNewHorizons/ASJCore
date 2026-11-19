## ASJCore (fork) - API/library mod for Minecraft 1.7.10
This is the [NTNewHorizons](https://github.com/NTNewHorizons/ASJCore) fork of [ASJCore](https://github.com/Nine-Circles-of-Coding/ASJCore) by AlexSocol (originally hosted on [Bitbucket](https://bitbucket.org/AlexSocol/asjcore/src/master/)).

Except helper and utility methods, it also includes Kotlin JDK 8 stdlib, GloomyFolken's HookLib, VamigA's WorldEngine, as well as a bunch of fixes and patches improving vanilla Minecraft.  
Upstream builds are available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/asjcore) and [Modrinth](https://modrinth.com/mod/asjcore).

### Changes from upstream
- **Removed features**
  - Orthographic projection view toggle (and the `orthoProjectionOn` config option)
  - Entity interaction toggle keybinding
- **Compatibility**
  - Hook replacers for the blindness fog and cape rotation fixes now also match renderer bytecode that was already transformed by other mods, instead of failing when local variable indices are shifted
  - Cape rotation fixes are no longer mandatory, so a missed match no longer breaks loading
  - HookLib's `methods.bin` dictionary moved to `assets/asjcore/hooklib/methods.bin`
- **Thread safety**
  - ASJ class transformer state is now thread-local. Previously, concurrent `transform()` calls (e.g. from ForgeMultipart's `ASMMixinCompiler`) could return another class's bytes, causing `NoClassDefFoundError: ... (wrong name: ...)`
- **Build**
  - The patched Forge artifact is stored in `gradle/` and automatically restored into `build/dirtyArtifacts` before compilation, so it survives `gradlew clean` and no manual restore step is needed
  - `META-INF/versions/**` is excluded from built jars, fixing development workspace startup

### How to compile for yourself:
1. Clone or download repository
2. Run those commands in terminal:
   1. `gradlew setupDecompWorkspace`
   2. `gradlew build`
3. Mod .jar files would be in `build/libs`

For the second and other times you want to compile the mod, only the `gradlew build` command is required.
