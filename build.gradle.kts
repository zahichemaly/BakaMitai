// Top-level build file. Declarative only: every plugin is declared here with `apply false` so it is on the
// build classpath for the modules and for the build-logic convention plugins.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.detekt) apply false
}
