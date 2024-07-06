plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm("desktop")
}

dependencies {
    commonMainImplementation(libs.kotlin.serialization)
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.org.json)
    commonMainImplementation(libs.jackson.xml)
    commonTestImplementation(libs.junit)
}