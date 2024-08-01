import org.gradle.api.tasks.Delete
import java.util.Properties

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.kgp)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.android.kotlin) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.jetbrains.compose) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

        maven {
            url = uri("https://maven.pkg.github.com/pebble-dev/libpebblecommon")
            credentials {
                val properties = Properties()
                if (rootProject.file("local.properties").canRead()) {
                    properties.load(rootProject.file("local.properties").inputStream())
                }

                // Set in local.properties
                username = System.getenv("GITHUB_ACTOR") ?: properties.getProperty("GITHUB_ACTOR", null)
                // github username
                password = System.getenv("GITHUB_TOKEN") ?: properties.getProperty("GITHUB_TOKEN", null)
                // personal access token
                if (username == null || password == null) error("Set github username and token in local.properties! (GITHUB_ACTOR and GITHUB_TOKEN)")
            }
        }

        mavenLocal()
    }
}

rootProject.buildDir = file("../build")
subprojects {
    project.buildDir = file("${rootProject.buildDir}/${project.name}")
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
