package co.touchlab.skie.plugin.generator.internal.`typealias`

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec

internal class TypeAliasGenerator(
    private val skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
    configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val areTypeAliasesExported = SkieFeature.FqNames in configuration.enabledFeatures

    private val baseTypeAliasContainerName = "Skie"

    private val publicTypeAliasContainerName = TypeSwiftModel.StableFqNameNamespace.removeSuffix(".")

    override fun execute() {
        skieContext.module.file("SkieTypeAliases") {
            addTypeAliasContainer(descriptorProvider)
            addBaseTypeAliasContainerTypeAlias()
        }
    }

    context(SwiftModelScope)
    private fun FileSpec.Builder.addTypeAliasContainer(descriptorProvider: DescriptorProvider) {
        addType(
            TypeSpec.enumBuilder(DeclaredTypeName.qualifiedLocalTypeName(publicTypeAliasContainerName))
                .addModifiers(Modifier.PUBLIC)
                .addTypeAliases(descriptorProvider)
                .build()
        )
    }

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addTypeAliases(descriptorProvider: DescriptorProvider): TypeSpec.Builder =
        this.apply {
            addClassTypeAliases(descriptorProvider)
            addFileTypeAliases(descriptorProvider)
        }

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addClassTypeAliases(descriptorProvider: DescriptorProvider) {
        descriptorProvider.transitivelyExposedClasses.forEach {
            addTypeAlias(it.swiftModel)
        }
    }

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addFileTypeAliases(descriptorProvider: DescriptorProvider) {
        descriptorProvider.exposedFiles.forEach {
            addTypeAlias(it.swiftModel)
        }
    }

    context(SwiftModelScope)
    private fun TypeSpec.Builder.addTypeAlias(swiftModel: TypeSwiftModel) {
        addType(
            TypeAliasSpec.builder(
                name = swiftModel.stableFqName.removePrefix(TypeSwiftModel.StableFqNameNamespace),
                type = DeclaredTypeName.qualifiedLocalTypeName(swiftModel.fqName),
            )
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

        (swiftModel as? KotlinTypeSwiftModel)?.bridge?.let { bridge ->
            addType(
                TypeAliasSpec.builder(
                    name = bridge.stableFqName.removePrefix(TypeSwiftModel.StableFqNameNamespace),
                    type = DeclaredTypeName.qualifiedLocalTypeName(bridge.fqName),
                )
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
        }
    }

    context(SwiftModelScope)
    private fun FileSpec.Builder.addBaseTypeAliasContainerTypeAlias() {
        val builder = TypeAliasSpec.builder(
            name = baseTypeAliasContainerName,
            type = DeclaredTypeName.qualifiedLocalTypeName(publicTypeAliasContainerName),
        )

        if (areTypeAliasesExported) {
            builder.addModifiers(Modifier.PUBLIC)
        }

        addType(builder.build())
    }
}
