import io.rebble.cobble.build.Repository
import java.util.Properties

plugins {
    alias(libs.plugins.kapt)
    alias(libs.plugins.serialization)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.android.application)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.reader().use { reader ->
        localProperties.load(reader)
    }
}

val flutterRoot = localProperties.getProperty("flutter.sdk")
        ?: throw GradleException("Flutter SDK not found. Define location with flutter.sdk in the local.properties file.")

val flutterVersionCode = localProperties.getProperty("flutter.versionCode") ?: "1"
val flutterVersionName = localProperties.getProperty("flutter.versionName") ?: "1.0"
//apply from: "$flutterRoot/packages/flutter_tools/gradle/flutter.gradle"
apply(from = "$flutterRoot/packages/flutter_tools/gradle/flutter.gradle")

android {
    if (System.getenv("ANDROID_NDK_HOME") != null) {
        ndkPath = System.getenv("ANDROID_NDK_HOME")
    }
    compileSdk = 34

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin")
        }
    }

    defaultConfig {
        applicationId = "io.rebble.cobble"
        minSdk = 29
        targetSdk = 34
        versionCode = flutterVersionCode.toInt()
        versionName = flutterVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        manifestPlaceholders["pebbleKitProviderAuthority"] = "io.rebble.cobble.provider"

        buildConfigField("String", "COMMIT_HASH", "\"${Repository.getShortCommitHash()}\"")
        buildConfigField("String", "BRANCH_NAME", "\"${Repository.getBranchName()}\"")
    }

    signingConfigs {
        create("release") {
            keyAlias = "upload"
            keyPassword = System.getenv("ALIAS_PASSWORD")
            storeFile = file("../key.jks")
            storePassword = System.getenv("KEY_PASSWORD")
        }
        create("nightly") {
            keyAlias = "key0"
            keyPassword = System.getenv("ALIAS_PASSWORD")
            storeFile = file("../key.jks")
            storePassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            if (System.getenv("NIGHTLY") == "true") {
                signingConfig = signingConfigs.getByName("nightly")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    namespace = "io.rebble.cobble"
//    lint {
//        isCheckReleaseBuilds = false
//        disable("InvalidPackage")
//    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalUnsignedTypes",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}

project.extensions.getByName("flutter").apply {
    this::class.java.getMethod("source", String::class.java).invoke(this, "../..")
}

val coroutinesVersion = "1.8.1"
val lifecycleVersion = "2.8.2"
val timberVersion = "5.0.1"
val androidxCoreVersion = "1.13.1"
val workManagerVersion = "2.9.0"
val okioVersion = "3.9.0"

val junitVersion = "4.13.2"
val androidxTestVersion = "1.5.0"

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.libpebblecommon)
    implementation(libs.kotlin.reflect)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    implementation("com.jakewharton.timber:timber:$timberVersion")
    implementation("androidx.core:core-ktx:$androidxCoreVersion")
    implementation("androidx.work:work-runtime-ktx:$workManagerVersion")
    implementation("com.squareup.okio:okio:$okioVersion")
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.datetime)
    implementation(libs.dagger)
    implementation(libs.uuid)
    implementation(project(":pebble_bt_transport"))
    implementation(project(":shared"))
    kapt(libs.dagger.compiler)

    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test:runner:$androidxTestVersion")
    androidTestImplementation("androidx.test:rules:$androidxTestVersion")
}

android.buildTypes.getByName("release").ndk.debugSymbolLevel = "FULL"
