repositories {
    mavenCentral()
    google()
    jcenter()
}

plugins {
    `kotlin-dsl`
    id("com.github.gmazzo.buildconfig") version "1.6.2"
}

val kotlinVersion = "1.4.10"
val androidGradlePluginVersion = "4.0.1"

buildConfig {
    buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
    buildConfigField("String", "AGP_VERSION", "\"$androidGradlePluginVersion\"")
}

dependencies {
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
