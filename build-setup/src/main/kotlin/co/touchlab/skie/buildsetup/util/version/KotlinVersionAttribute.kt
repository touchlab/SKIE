package co.touchlab.skie.buildsetup.util.version

import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.MultipleCandidatesDetails
import javax.inject.Inject

interface KotlinVersionAttribute : Named {

    companion object {

        const val attributeName: String = "co.touchlab.skie.kotlin.compiler.version"

        val attribute: Attribute<KotlinVersionAttribute> = Attribute.of(attributeName, KotlinVersionAttribute::class.java)

        fun registerIn(dependencies: DependencyHandler, supportedKotlinVersions: List<SupportedKotlinVersion>) {
            dependencies.attributesSchema.attribute(attribute) {
                disambiguationRules.add(DisambiguationRule::class.java) {
                    params(supportedKotlinVersions)
                }
            }
        }
    }

    class DisambiguationRule @Inject constructor(
        private val supportedKotlinVersions: List<SupportedKotlinVersion>,
    ) : AttributeDisambiguationRule<KotlinVersionAttribute> {

        override fun execute(details: MultipleCandidatesDetails<KotlinVersionAttribute>) {
            val candidateVersions = details.candidateValues
                .mapNotNull { candidate -> supportedKotlinVersions.find { it.name.toString() == candidate.name }?.let { it to candidate } }
                .sortedBy { it.first.name }

            val consumerValue = details.consumerValue?.name ?: candidateVersions.maxByOrNull { it.first.name }?.first?.compilerVersion?.toString()

            val correctCandidate = candidateVersions.find { candidate ->
                consumerValue in candidate.first.supportedVersions.map { it.toString() }
            }

            if (correctCandidate != null) {
                details.closestMatch(correctCandidate.second)
            } else {
                error("Could not find a Kotlin compiler version matching the selected Kotlin version ${consumerValue}! Candidates: ${candidateVersions.joinToString { it.second.name }}")
            }
        }
    }
}
