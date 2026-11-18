import com.android.build.api.dsl.ApplicationExtension
import extension.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * `bakamitai.android.application`: the Android application with Compose (AGP 9 built-in Kotlin).
 * Sets SDK levels, the JVM target, release optimization and Compose compiler reports. The module script
 * only adds its namespace, application id, version and dependencies.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("android-application").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)

        extensions.configure<ApplicationExtension> {
            compileSdk {
                version = release(COMPILE_SDK)
            }
            defaultConfig {
                minSdk = MIN_SDK
                targetSdk = TARGET_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            buildTypes.getByName("release") {
                optimization {
                    enable = true
                    packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            buildFeatures {
                compose = true
                buildConfig = true
            }
        }

        // Stability audit (best-practices §2.3): app/build/compose_compiler/*-classes.txt / *-composables.txt
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            reportsDestination.set(layout.buildDirectory.dir("compose_compiler"))
            metricsDestination.set(layout.buildDirectory.dir("compose_compiler"))
        }
    }

    private companion object {
        const val COMPILE_SDK = 37
        const val TARGET_SDK = 37
        const val MIN_SDK = 27
    }
}
