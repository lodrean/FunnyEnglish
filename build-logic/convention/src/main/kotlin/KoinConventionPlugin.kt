import org.gradle.api.Plugin
import org.gradle.api.Project

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Koin dependencies are added manually in module build files
        // This plugin serves as a marker and can be extended later
    }
}
