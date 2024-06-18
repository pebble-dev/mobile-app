import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("androidx.room")
    id("com.google.devtools.ksp")
}

val uuidVersion = "0.8.4"
val roomVersion = "2.7.0-alpha04"
val roomSqliteVersion = "2.5.0-alpha04"
val libpebblecommonVersion = "0.1.20"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
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
            implementation("com.benasher44:uuid:$uuidVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation("androidx.room:room-runtime:$roomVersion")
            implementation("androidx.sqlite:sqlite-bundled:$roomSqliteVersion")
            implementation("io.rebble.libpebblecommon:libpebblecommon:$libpebblecommonVersion")
        }
        androidMain.dependencies {

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
