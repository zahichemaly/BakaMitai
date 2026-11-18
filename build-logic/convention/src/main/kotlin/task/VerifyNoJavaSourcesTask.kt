package task

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Fails when any `*.java` file is found in [sources]. */
@CacheableTask
abstract class VerifyNoJavaSourcesTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val javaFiles = sources.asFileTree.matching { include("**/*.java") }.files
        check(javaFiles.isEmpty()) {
            "Java sources are not allowed:\n" + javaFiles.joinToString("\n")
        }
        report.get().asFile.writeText("OK")
    }
}
