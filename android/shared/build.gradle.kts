import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("androidx.room")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "1.7.0"
}

val uuidVersion = "0.8.4"
val roomVersion = "2.7.0-alpha04"
val roomSqliteVersion = "2.5.0-alpha04"
val libpebblecommonVersion = "0.1.20"
val timberVersion = "5.0.1"
val androidxVersion = "1.13.1"
val koinVersion = "3.2.0"
val rruleVersion = "1.0.3"

kotlin {
    androidTarget {
        compilations.all {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_1_8
            }
        }
    }
    
    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api("io.insert-koin:koin-core:$koinVersion")

            implementation("com.benasher44:uuid:$uuidVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation("androidx.room:room-runtime:$roomVersion")
            implementation("androidx.sqlite:sqlite-bundled:$roomSqliteVersion")
            implementation("io.rebble.libpebblecommon:libpebblecommon:$libpebblecommonVersion")
        }
        androidMain.dependencies {
            implementation("io.insert-koin:koin-android:$koinVersion")
            implementation("androidx.core:core-ktx:$androidxVersion")
            implementation("com.jakewharton.timber:timber:$timberVersion")
            implementation("com.github.PhilJay:RRule:$rruleVersion")
        }
        commonTest.dependencies {
            implementation(kotlin("test-common"))
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

android {
    namespace = "io.rebble.cobble.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation("com.google.firebase:protolite-well-known-types:18.0.0")
    add("kspCommonMainMetadata", "androidx.room:room-compiler:$roomVersion")
}
