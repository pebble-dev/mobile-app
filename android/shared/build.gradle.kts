import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

val roomSqliteVersion = "2.5.0-alpha04"
val timberVersion = "5.0.1"
val androidxVersion = "1.13.1"
val koinVersion = "3.2.0"
val rruleVersion = "1.0.3"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = JvmTarget.JVM_1_8.target
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
            api(libs.koin.core)
            api(libs.kotlinx.serialization.core)

            implementation(libs.uuid)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.room.runtime)
            implementation(libs.libpebblecommon)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
        }
        androidMain.dependencies {
            implementation("io.insert-koin:koin-android:$koinVersion")
            implementation("androidx.core:core-ktx:$androidxVersion")
            implementation("com.jakewharton.timber:timber:$timberVersion")
            implementation("com.github.PhilJay:RRule:$rruleVersion")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

android {
    namespace = "io.rebble.cobble.shared"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        buildConfigField("Boolean", "VERBOSE_BT", "false") // Will spam logs and slow down bluetooth
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation("com.google.firebase:protolite-well-known-types:18.0.0")
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
}
