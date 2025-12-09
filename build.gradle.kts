// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val gradle = "8.13.0"
    val kotlin = "2.2.0"
    val ksp = "2.2.10-2.0.2"
    val gms = "4.4.1"
    id("com.android.application") version gradle apply false
    id("com.android.library") version gradle apply false
    id("org.jetbrains.kotlin.android") version kotlin apply false
    id("org.jetbrains.kotlin.plugin.compose") version kotlin apply false
    id("com.google.devtools.ksp") version ksp apply false
    id("com.google.gms.google-services") version gms apply false
}
