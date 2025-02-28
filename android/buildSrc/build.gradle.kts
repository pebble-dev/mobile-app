plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}