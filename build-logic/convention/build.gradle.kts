plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.bakamitai.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("detekt") {
            id = libs.plugins.bakamitai.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }
        register("kotlinOnly") {
            id = libs.plugins.bakamitai.kotlin.only.get().pluginId
            implementationClass = "KotlinOnlyConventionPlugin"
        }
    }
}
