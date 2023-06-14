package co.touchlab.skie.plugin.license.util

import co.touchlab.skie.plugin.license.SkieLicense
import java.nio.file.Path

data class LicenseWithPath(val license: SkieLicense, val path: Path)
