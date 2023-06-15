package co.touchlab.skie.plugin.analytics.crash

import co.touchlab.skie.util.Environment
import com.bugsnag.Bugsnag

object BugsnagFactory {

    fun create(
        skieVersion: String,
        type: Type,
        environment: Environment,
    ): Bugsnag =
        Bugsnag("", false)
            .apply {
                setAutoCaptureSessions(false)
                setAppVersion(skieVersion)
                setAppType(type.name)
                setReleaseStage(environment.name)
                setProjectPackages("co.touchlab.skie", "org.jetbrains.kotlin")

                startSession()
            }

    enum class Type {
        Gradle, Compiler
    }
}
