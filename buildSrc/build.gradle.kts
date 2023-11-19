repositories {
    mavenCentral()
    google()
}

plugins {
    `kotlin-dsl`
    id("com.github.gmazzo.buildconfig") version "3.0.2"
}

val kotlinVersion = "1.9.20"
val androidGradlePluginVersion = "8.1.4"

buildConfig {
    buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
    buildConfigField("String", "AGP_VERSION", "\"$androidGradlePluginVersion\"")
}

dependencies {
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}
