package co.touchlab.skie.gradle.version

class GradleApiVersionAxis(
    override val labels: List<GradleApiVersion>,
): DependencyMatrix.Axis<GradleApiVersion> {
    override val name: String = "gradle"

    override fun targetNameFor(label: GradleApiVersion): String = label.gradleVersion.version
}
