@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.debug

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.KirPhase
import co.touchlab.skie.phases.descriptorProvider
import co.touchlab.skie.phases.mapper
import co.touchlab.skie.phases.objCExportedInterfaceProvider
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe

object VerifyDescriptorProviderConsistencyPhase : KirPhase {

    context(context: KirPhase.Context)
    override fun isActive(): Boolean = context.run { SkieConfigurationFlag.Debug_VerifyDescriptorProviderConsistency.isEnabled }

    context(context: KirPhase.Context)
    override suspend fun execute() {
        val objCExportedInterface = context.objCExportedInterfaceProvider.objCExportedInterface

        val errors = listOfNotNull(
            context.descriptorProvider.exposedClasses.shouldMatchExposed(objCExportedInterface.generatedClasses) { fqNameUnsafe.asString() },
            context.descriptorProvider.exposedCategoryMembers.shouldMatch(objCExportedInterface.categoryMembers.values.flatten().toSet()) { fqNameUnsafe.asString() },
            context.descriptorProvider.exposedTopLevelMembers.shouldMatch(objCExportedInterface.topLevel.values.flatten().toSet()) { fqNameUnsafe.asString() },
            context.descriptorProvider.exposedFiles.shouldMatch(objCExportedInterface.topLevel.keys) { name ?: "<Unknown file>" },
        )

        if (errors.isNotEmpty()) {
            error(
                "Descriptor provider and ObjC exported interface are inconsistent:\n" +
                    errors.joinToString("\n") { "    $it" },
            )
        }
    }

    context(context: KirPhase.Context)
    private fun Set<ClassDescriptor>.shouldMatchExposed(other: Set<ClassDescriptor>, fqName: ClassDescriptor.() -> String): String? {
        // The Kotlin compiler (incorrectly) includes non-exported classes in certain cases - for example, "internal" exceptions from Java.
        // SKIE intentionally excludes these classes, so the filter is needed before comparing the sets.
        val exposedOther = other.filter { context.mapper.shouldBeExposed(it) }.toSet()

        return this.shouldMatch(exposedOther, fqName)
    }

    private fun <T> Set<T>.shouldMatch(other: Set<T>, fqName: T.() -> String): String? {
        val missing = other - this
        val extra = this - other

        return if (missing.isNotEmpty() || extra.isNotEmpty()) {
            "Missing: ${missing.map { it.fqName() }}, Extra: ${extra.map { it.fqName() }}"
        } else {
            null
        }
    }
}
