package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isExported

object CreateStableNameTypeAliasesPhase : SirPhase {

    context(SirPhase.Context)
    private val shouldGenerateFileForEachExportedClass: Boolean
        get() = SkieConfigurationFlag.Debug_GenerateFileForEachExportedClass.isEnabled

    context(SirPhase.Context)
    private val useStableTypeAliases: Boolean
        get() = SkieConfigurationFlag.Debug_UseStableTypeAliases.isEnabled

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses
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
                namespaceProvider.getNamespaceExtension(kirClass)
            } else {
                namespaceProvider.getNamespaceClass(kirClass)
            },
            visibility = SirVisibility.PublicButHidden,
            isReplaced = true,
        ) {
            kirClass.originalSirClass.defaultType.withFqName()
        }

        if (useStableTypeAliases && kirClass.originalSirClass.internalTypeAlias == null) {
            kirClass.originalSirClass.internalTypeAlias = typeAlias
        }
    }
}

val KirClass.hasStableNameTypeAlias: Boolean
    get() = originalSirClass.isExported && configuration[ClassInterop.StableTypeAlias]
