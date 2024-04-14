package co.touchlab.skie.test.runner

import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode

data class SkieTestMatrixSource(
    val targets: MutableList<KotlinTarget>,
    val presets: MutableList<KotlinTarget.Preset>,
    val configurations: MutableList<BuildConfiguration>,
    val linkModes: MutableList<LinkMode>,
    val kotlinVersions: MutableList<KotlinVersion>,
)
