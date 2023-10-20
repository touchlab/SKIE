import co.touchlab.skie.gradle.BuildSetupScope
import org.gradle.api.initialization.Settings

inline fun Settings.buildSetup(configure: BuildSetupScope.() -> Unit) {
    configure(BuildSetupScope(this, emptyList(), null))
}
