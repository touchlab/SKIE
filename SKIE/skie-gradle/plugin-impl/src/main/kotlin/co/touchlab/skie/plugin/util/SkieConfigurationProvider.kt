package co.touchlab.skie.plugin.util

import co.touchlab.skie.util.directory.SkieBuildDirectory
import java.io.File
import org.gradle.api.provider.Provider

val Provider<SkieBuildDirectory>.skieConfiguration: Provider<File>
    get() = map { it.skieConfiguration }
