import dev.detekt.gradle.extensions.DetektExtension
import extension.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * `bakamitai.detekt`: Detekt with ktlint formatting and Compose rules, configured from
 * `config/detekt/detekt.yml` on top of Detekt's defaults. No baseline: findings must be fixed.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("detekt").get().get().pluginId)
        extensions.configure<DetektExtension> {
            buildUponDefaultConfig.set(true)
            parallel.set(true)
            // Every source set (main, test, androidTest, …), not only Detekt's default main/test dirs.
            source.setFrom(layout.projectDirectory.dir("src"))
            config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        }
        dependencies {
            add("detektPlugins", libs.findLibrary("detekt-formatting").get())
            add("detektPlugins", libs.findLibrary("compose-rules-detekt").get())
        }
    }
}
