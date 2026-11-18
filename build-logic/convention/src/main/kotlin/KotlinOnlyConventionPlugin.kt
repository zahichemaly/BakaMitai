import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import task.VerifyNoJavaSourcesTask

/**
 * `bakamitai.kotlin.only`: registers `verifyNoJavaSources` and hooks it into `check`, so a `.java`
 * file anywhere under `src/` fails the build.
 */
class KotlinOnlyConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(LifecycleBasePlugin::class.java)
            val verify = tasks.register<VerifyNoJavaSourcesTask>("verifyNoJavaSources") {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                description = "Fails when the module contains Java sources."
                sources.from(layout.projectDirectory.dir("src"))
                report.set(layout.buildDirectory.file("reports/verifyNoJavaSources.txt"))
            }
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { dependsOn(verify) }
        }
    }
}
