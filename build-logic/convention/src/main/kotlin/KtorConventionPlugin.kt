import org.gradle.api.Plugin
import org.gradle.api.Project

class KtorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Ktor dependencies are added manually in module build files
        // This plugin serves as a marker and can be extended later
    }
}
