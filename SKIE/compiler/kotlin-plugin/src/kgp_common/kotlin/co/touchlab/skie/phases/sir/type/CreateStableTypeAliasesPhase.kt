package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isExported

class CreateStableNameTypeAliasesPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val shouldGenerateFileForEachExportedClass: Boolean =
        SkieConfigurationFlag.Debug_GenerateFileForEachExportedClass in context.skieConfiguration.enabledConfigurationFlags

    private val useStableTypeAliases: Boolean =
        SkieConfigurationFlag.Debug_UseStableTypeAliases in context.skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.allClasses
            .filter { it.hasStableNameTypeAlias || shouldGenerateFileForEachExportedClass }
            .forEach {
                createTypeAlias(it)
            }
    }

    context(SirPhase.Context)
    private fun createTypeAlias(kirClass: KirClass) {
        val typeAlias = SirTypeAlias(
            baseName = "Kotlin",
            parent = if (shouldGenerateFileForEachExportedClass) {
                classNamespaceProvider.getNamespace(kirClass)
            } else {
                classNamespaceProvider.getNamespaceClass(kirClass)
            },
            visibility = SirVisibility.PublicButReplaced,
        ) {
            kirClass.originalSirClass.defaultType.withFqName()
        }

        if (useStableTypeAliases && kirClass.originalSirClass.internalTypeAlias == null) {
            kirClass.originalSirClass.internalTypeAlias = typeAlias
        }
    }
}

context(SirPhase.Context)
val KirClass.hasStableNameTypeAlias: Boolean
    get() = this.originalSirClass.isExported &&
        this.getConfiguration(ClassInterop.StableTypeAlias)
