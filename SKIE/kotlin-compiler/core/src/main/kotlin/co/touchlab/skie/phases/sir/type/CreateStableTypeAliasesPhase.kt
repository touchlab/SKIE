package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.isExported

object CreateStableNameTypeAliasesPhase : SirPhase {

    context(context: SirPhase.Context)
    private val shouldGenerateFileForEachExportedClass: Boolean
        get() = context.run { SkieConfigurationFlag.Debug_GenerateFileForEachExportedClass.isEnabled }

    context(context: SirPhase.Context)
    private val useStableTypeAliases: Boolean
        get() = context.run { SkieConfigurationFlag.Debug_UseStableTypeAliases.isEnabled }

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.kirProvider.kotlinClasses
            .filter { it.hasStableNameTypeAlias || shouldGenerateFileForEachExportedClass }
            .forEach {
                createTypeAlias(it)
            }
    }

    context(context: SirPhase.Context)
    private fun createTypeAlias(kirClass: KirClass) {
        val typeAlias = SirTypeAlias(
            baseName = "Kotlin",
            parent = if (shouldGenerateFileForEachExportedClass) {
                context.namespaceProvider.getNamespaceExtension(kirClass)
            } else {
                context.namespaceProvider.getNamespaceClass(kirClass)
            },
            isReplaced = true,
            isHidden = true,
        ) {
            kirClass.originalSirClass.toFqNameType()
        }

        if (useStableTypeAliases && kirClass.originalSirClass.internalTypeAlias == null) {
            kirClass.originalSirClass.internalTypeAlias = typeAlias
        }
    }
}

val KirClass.hasStableNameTypeAlias: Boolean
    get() = originalSirClass.isExported && configuration[ClassInterop.StableTypeAlias]
