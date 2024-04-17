@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.debug

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.CompilerDependentKirPhase
import co.touchlab.skie.phases.KirPhase
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe

object VerifyDescriptorProviderConsistencyPhase : CompilerDependentKirPhase {

    context(KirPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Debug_VerifyDescriptorProviderConsistency.isEnabled

    context(CompilerDependentKirPhase.Context)
    override suspend fun execute() {
        val objCExportedInterface = objCExportedInterfaceProvider.objCExportedInterface

        val errors = listOfNotNull(
            descriptorProvider.exposedClasses.shouldMatch(objCExportedInterface.generatedClasses) { fqNameUnsafe.asString() },
            descriptorProvider.exposedCategoryMembers.shouldMatch(objCExportedInterface.categoryMembers.values.flatten().toSet()) { fqNameUnsafe.asString() },
            descriptorProvider.exposedTopLevelMembers.shouldMatch(objCExportedInterface.topLevel.values.flatten().toSet()) { fqNameUnsafe.asString() },
            descriptorProvider.exposedFiles.shouldMatch(objCExportedInterface.topLevel.keys) { name ?: "<Unknown file>" },
        )

        if (errors.isNotEmpty()) {
            error(
                "Descriptor provider and ObjC exported interface are inconsistent:\n" +
                    errors.joinToString("\n") { "    $it" },
            )
        }
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
