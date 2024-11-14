package co.touchlab.skie.gradle

import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.MultipleCandidatesDetails
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import javax.inject.Inject

interface KotlinCompilerVersion : Named {

    companion object {

        val attribute: Attribute<KotlinCompilerVersion> = Attribute.of("co.touchlab.skie.kotlin.compiler.version", KotlinCompilerVersion::class.java)

        fun registerIn(dependencies: DependencyHandler, currentKotlinVersion: String) {
            dependencies.attributesSchema.attribute(attribute) {
                disambiguationRules.add(DisambiguationRule::class.java) {
                    params(currentKotlinVersion)
                }
            }
        }
    }

    class DisambiguationRule @Inject constructor(
        currentKotlinVersion: String,
    ) : AttributeDisambiguationRule<KotlinCompilerVersion> {
        private val currentKotlinVersion = KotlinToolingVersion(currentKotlinVersion)

        override fun execute(details: MultipleCandidatesDetails<KotlinCompilerVersion>) {
            val correctCandidate = details.candidateValues.lastOrNull {
                KotlinToolingVersion(it.name) < currentKotlinVersion
            }

            if (correctCandidate != null) {
                details.closestMatch(correctCandidate)
            } else {
                // This should've already been caught by SKIE Plugin Loader, but we'll let the user know just in case.
                log.error("Could not find a Kotlin compiler version matching the current Kotlin version ($currentKotlinVersion)! Candidates: ${details.candidateValues.joinToString { it.name }}")
            }
        }

        companion object {

            val log: Logger = Logging.getLogger("KotlinCompilerVersion.DisambiguationRule")
        }
    }
}
