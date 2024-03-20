// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val gradle = "8.3.0"
    val kotlin = "1.9.22"
    val ksp = "1.9.22-1.0.16"
    val gms = "4.4.1"
    id("com.android.application") version gradle apply false
    id("com.android.library") version gradle apply false
    id("org.jetbrains.kotlin.android") version kotlin apply false
    id("com.google.devtools.ksp") version ksp apply false
    id("com.google.gms.google-services") version gms apply false
}
