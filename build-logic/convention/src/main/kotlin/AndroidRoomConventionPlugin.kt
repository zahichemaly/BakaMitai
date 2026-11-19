import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import extension.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * `bakamitai.android.room`: Room with its compiler on KSP. KSP processes only Room here: Koin keeps its compiler
 * plugin. Schemas are exported to `app/schemas/` (committed) and Room generates Kotlin.
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("ksp").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("room").get().get().pluginId)

        extensions.configure<RoomExtension> {
            schemaDirectory(layout.projectDirectory.dir("schemas").asFile.path)
        }
        extensions.configure<KspExtension> {
            arg("room.generateKotlin", "true")
        }
        dependencies {
            add("implementation", libs.findLibrary("androidx-room-runtime").get())
            add("ksp", libs.findLibrary("androidx-room-compiler").get())
        }
    }
}
