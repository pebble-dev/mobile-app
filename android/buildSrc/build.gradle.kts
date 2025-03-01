plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}