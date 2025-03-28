import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.jetbrains.kotlinx.atomicfu)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = JvmTarget.JVM_17.target
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
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
            api(libs.koin.core)
            api(libs.kotlinx.serialization.core)

            //XXX: Workaround for https://github.com/Kotlin/kotlinx-atomicfu/issues/469
            implementation(libs.jetbrains.kotlinx.atomicfu)
            implementation(libs.koin.compose)
            implementation(libs.uuid)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.room.runtime)
            implementation(libs.libpebblecommon)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentnegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(libs.compose.navigation)
            implementation(libs.compose.viewmodel)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.reorderable)
            api("io.github.kevinnzou:compose-webview-multiplatform:1.9.40")
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.core.ktx)
            implementation(libs.timber)
            implementation(libs.rrule)
            implementation(libs.androidx.security.crypto.ktx)
            api(project(":pebblekit_android"))
            implementation(project(":speex_codec"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.koin.test)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(libs.protolite.wellknowntypes)
    implementation(libs.androidx.security.crypto.ktx)
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
}