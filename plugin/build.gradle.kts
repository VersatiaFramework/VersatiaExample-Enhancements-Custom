import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "com.github.marcoral.versatia"
version = "0.0.1"

val apiVersion = "0.0.1"

repositories {
    // VersatiaAPI is consumed from the local Maven repository (`./gradlew publishToMavenLocal` in its checkout)
    mavenLocal()
    mavenCentral()
    // Needed to resolve the Paper API that VersatiaAPI exports
    maven("https://repo.papermc.io/repository/maven-public/")
}

// Paper API and the Kotlin standard library are inherited from VersatiaAPI; nothing is bundled,
// on the server these classes come from VersatiaCore (see plugin.yml)
dependencies {
    implementation("com.github.marcoral.versatia:VersatiaAPI:$apiVersion")
    ksp("com.github.marcoral.versatia:VersatiaAPI-ksp:$apiVersion")
// On the server the annotation and the processor come from TimingLibrary through depend:
compileOnly(project(":library"))
}

// Paper 1.21.4 requires Java 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
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

// Machine-specific settings; see README.md
val localProperties: Provider<Properties> = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .map { text -> Properties().apply { load(text.reader()) } }

val pluginsDir: Provider<String> = providers.gradleProperty("pluginsDir")
    .orElse(localProperties.map { it.getProperty("pluginsDir") })

tasks.register<Copy>("deployPlugin") {
    group = "versatia"
    description = "Copies the plugin JAR to the server plugins/ directory (pluginsDir from local.properties)."
    // Local copy: the onlyIf lambda must not capture script fields (configuration cache)
    val targetDir = pluginsDir
    onlyIf("pluginsDir is not set - see local.properties.example") { targetDir.isPresent }
    from(tasks.jar)
    into(targetDir.orElse(layout.buildDirectory.dir("deploy").map { it.asFile.path }))
}

tasks.build {
    finalizedBy(tasks.named("deployPlugin"))
}
