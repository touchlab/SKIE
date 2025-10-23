package co.touchlab.skie.gradle

import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.MultipleCandidatesDetails
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import javax.inject.Inject

interface KotlinCompilerVersionAttribute : Named {

    companion object {

        val attribute: Attribute<KotlinCompilerVersionAttribute> = Attribute.of("co.touchlab.skie.kotlin.compiler.version", KotlinCompilerVersionAttribute::class.java)

        fun registerIn(dependencies: DependencyHandler, currentKotlinVersion: String) {
            dependencies.attributesSchema.attribute(attribute) {
                disambiguationRules.add(DisambiguationRule::class.java) {
                    params(currentKotlinVersion)
                }
            }
        }
    }

    // WIP use KotlinCompilerVersionEnum to get the correct version
    class DisambiguationRule @Inject constructor(
        currentKotlinVersion: String,
    ) : AttributeDisambiguationRule<KotlinCompilerVersionAttribute> {
        private val currentKotlinVersion = KotlinToolingVersion(currentKotlinVersion)

        override fun execute(details: MultipleCandidatesDetails<KotlinCompilerVersionAttribute>) {
            val candidateVersions = details.candidateValues
                .map { KotlinToolingVersion(it.name) to it }
                .sortedBy { it.first }

            val correctCandidate = candidateVersions
                .lastOrNull { it.first <= currentKotlinVersion }
                ?.second

            if (correctCandidate != null) {
                details.closestMatch(correctCandidate)
            } else {
                // This should've already been caught by SKIE Plugin Loader, but we'll let the user know just in case.
                log.error("Could not find a Kotlin compiler version matching the current Kotlin version ($currentKotlinVersion)! Candidates: ${candidateVersions.joinToString { it.second.name }}")
            }
        }

        companion object {

            val log: Logger = Logging.getLogger("KotlinCompilerVersion.DisambiguationRule")
        }
    }
}
