import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// A library plugin: no KSP, it only provides an annotation and a processor to other plugins
plugins {
    kotlin("jvm")
}

group = "com.github.marcoral.versatia"
version = "0.0.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("com.github.marcoral.versatia:VersatiaAPI:0.0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
