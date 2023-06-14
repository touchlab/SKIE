package co.touchlab.skie.gradle.version

import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

class KotlinToolingVersionAxis(
    override val labels: List<KotlinToolingVersion>,
): DependencyMatrix.Axis<KotlinToolingVersion> {
    override val name: String = "kgp"

    override fun targetNameFor(label: KotlinToolingVersion): String = label.toString()
}
