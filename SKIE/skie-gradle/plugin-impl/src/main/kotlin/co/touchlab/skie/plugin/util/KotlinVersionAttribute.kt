package co.touchlab.skie.plugin.util

import co.touchlab.skie.gradle_plugin_impl.BuildConfig
import co.touchlab.skie.plugin.KotlinCompilerVersion
import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.MultipleCandidatesDetails
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

interface KotlinVersionAttribute : Named {

    companion object {

        val attribute: Attribute<KotlinVersionAttribute> = Attribute.of(BuildConfig.SKIE_KOTLIN_VERSION_ATTRIBUTE_NAME, KotlinVersionAttribute::class.java)

        fun registerIn(dependencies: DependencyHandler) {
            dependencies.attributesSchema.attribute(attribute) {
                disambiguationRules.add(DisambiguationRule::class.java)
            }
        }
    }

    class DisambiguationRule : AttributeDisambiguationRule<KotlinVersionAttribute> {

        override fun execute(details: MultipleCandidatesDetails<KotlinVersionAttribute>) {
            val candidateVersions = details.candidateValues
                .mapNotNull { candidate -> KotlinCompilerVersion.entries.find { it.versionName == candidate.name }?.let { it to candidate } }
                .sortedBy { it.first.versionName }

            val consumerValue = details.consumerValue?.name ?: candidateVersions.maxByOrNull { it.first.versionName }?.first?.compilerVersion

            val correctCandidate = candidateVersions.find { candidate ->
                consumerValue in candidate.first.supportedVersions
            }

            if (correctCandidate != null) {
                details.closestMatch(correctCandidate.second)
            } else {
                // This should've already been caught by SKIE Plugin Loader, but we'll let the user know just in case.
                log.error("Could not find a Kotlin compiler version matching the selected Kotlin version ${consumerValue}! Candidates: ${candidateVersions.joinToString { it.second.name }}")
            }
        }

        companion object {

            val log: Logger = Logging.getLogger("KotlinVersionAttribute.DisambiguationRule")
        }
    }
}
