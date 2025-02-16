plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.kotlin)
}

android {
    namespace = "com.example.pebble_ble"
    compileSdk = 34

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        options.freeCompilerArgs.add("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
    }
}

val timberVersion = "4.7.1"
val coroutinesVersion = "1.8.0"
val okioVersion = "3.7.0"
val mockkVersion = "1.13.11"
val nordicBleVersion = "1.0.16"

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.libpebblecommon)
    implementation("com.jakewharton.timber:timber:$timberVersion")
    // for nordic ble
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("com.github.tony19:logback-android:3.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("com.squareup.okio:okio:$okioVersion")
    implementation(libs.kotlinx.serialization.json)



    implementation("no.nordicsemi.android.kotlin.ble:core:$nordicBleVersion")
    implementation("no.nordicsemi.android.kotlin.ble:server:$nordicBleVersion")
    implementation(project(":shared"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}