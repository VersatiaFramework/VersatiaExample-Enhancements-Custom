    # VersatiaExample-Enhancements-Custom

    An enhancement of your own: a library plugin defines `@Timed` and its processor, and another plugin uses the annotation to run a function every few ticks with injected parameters.

    Part of the [Versatia examples](https://github.com/VersatiaFramework/VersatiaExamples); documentation: [Defining your own enhancement](https://versatiaframework.github.io/VersatiaDocs/docs/internals/enhancements#defining-your-own-enhancement).

    **Start with** [`library/.../Timed.kt`](library/src/main/kotlin/com/github/marcoral/versatia/example/timing/Timed.kt) and [`TimedEnhancementProcessor.kt`](library/src/main/kotlin/com/github/marcoral/versatia/example/timing/TimedEnhancementProcessor.kt), then [`plugin/.../TimedPlugin.kt`](plugin/src/main/kotlin/com/github/marcoral/versatia/example/timing/plugin/TimedPlugin.kt).

    ## What the log shows

    ```
    [TimedEnhancementProcessor] scheduled heartbeat() every 100 ticks
heartbeat at ...
heartbeat at ...
    ```


## Things to try

- Open the plugin's generated descriptor: the element names the processor by its fully qualified
  name as a string, and records `@Timed(everyTicks = 100)` with its argument. The plugin never
  compiled against the processor class.
- Remove `TimingLibrary` from the server: the plugin fails to enable with `needs the enhancement
  processor ... which is not available`.

## Building

Requires JDK 21 and VersatiaAPI in your local Maven repository (`./gradlew publishToMavenLocal`
in its checkout); running it requires a Paper 1.21.4 server with VersatiaCore installed.

```bash
./gradlew build
```

To have the JAR copied to your server after every build, copy `local.properties.example` to
`local.properties` and set `pluginsDir`. The generated descriptor is in
`build/generated/ksp/main/kotlin`; open it to see exactly what the runtime executes.

## Layout

Two Gradle modules, one plugin each. `library` is a plain Bukkit plugin holding the annotation and
the processor; it needs only VersatiaAPI to compile. `plugin` compiles against the library with
`compileOnly(project(":library"))` and lists it under `depend:`.
