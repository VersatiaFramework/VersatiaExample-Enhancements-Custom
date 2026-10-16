import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import org.yaml.snakeyaml.Yaml

// SnakeYAML parses plugin.yml in checkPluginYml
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.yaml:snakeyaml:2.4")
    }
}

// A library plugin: no KSP, it only provides an annotation and a processor to other plugins
plugins {
    kotlin("jvm")
}

group = "com.github.marcoral.versatia"
version = "0.0.1"

// The JAR is named after the plugin, not after the Gradle module
base {
    archivesName = "TimingLibrary"
}

val versatiaVersion = "0.0.1"

// Paper API and the Kotlin standard library are inherited from VersatiaAPI; nothing is bundled,
// on the server these classes come from VersatiaCore (see plugin.yml)
dependencies {
    implementation("com.github.marcoral.versatia:VersatiaAPI:$versatiaVersion")
}

// Paper 1.21.4 requires Java 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

// Expands ${version} in plugin.yml
tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// Paper skips a plugin whose plugin.yml does not parse; fail the build instead
val checkPluginYml = tasks.register("checkPluginYml") {
    group = "verification"
    description = "Checks that plugin.yml parses and names the plugin, its main class and VersatiaCore."
    val pluginYml = layout.buildDirectory.file("resources/main/plugin.yml")
    dependsOn(tasks.processResources)
    inputs.file(pluginYml)
    doLast {
        val file = pluginYml.get().asFile
        val root = file.reader().use { Yaml().load<Any?>(it) } as? Map<*, *>
            ?: throw GradleException("$file is not a YAML mapping")
        val missing = listOf("name", "version", "main").filter { root[it] == null }
        if (missing.isNotEmpty()) throw GradleException("$file lacks $missing")
        if ("VersatiaCore" !in (root["depend"] as? List<*>).orEmpty())
            throw GradleException("$file must list VersatiaCore under depend:")
    }
}

tasks.check {
    dependsOn(checkPluginYml)
}

// Machine-specific settings; see README.md
val localProperties = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .map { text -> Properties().apply { load(text.reader()) } }

val pluginsDir: Provider<String> = providers.gradleProperty("pluginsDir")
    .orElse(localProperties.map { it.getProperty("pluginsDir") })

val deployPlugin = tasks.register<Copy>("deployPlugin") {
    group = "versatia"
    description = "Copies the plugin JAR to the server plugins/ directory (pluginsDir from local.properties)."
    // Local copy: the onlyIf lambda must not capture script fields (configuration cache)
    val targetDir = pluginsDir
    onlyIf("pluginsDir is not set - see local.properties.example") { targetDir.isPresent }
    from(tasks.jar)
    // Without the version, a newer build replaces the deployed JAR instead of sitting next to it
    val jarName = base.archivesName.map { "$it.jar" }
    rename { jarName.get() }
    into(targetDir.orElse(layout.buildDirectory.dir("deploy").map { it.asFile.path }))
}

tasks.build {
    finalizedBy(deployPlugin)
}
