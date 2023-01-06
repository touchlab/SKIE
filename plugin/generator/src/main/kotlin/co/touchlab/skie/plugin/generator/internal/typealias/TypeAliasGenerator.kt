package co.touchlab.skie.plugin.generator.internal.`typealias`

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec

internal class TypeAliasGenerator(
    private val skieContext: SkieContext,
    configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val areTypeAliasesExported = SkieFeature.FqNames in configuration.enabledFeatures

    private val baseTypeAliasContainerName = "Skie"

    private val publicTypeAliasContainerName = KotlinTypeSwiftModel.StableFqNameNamespace.removeSuffix(".")

    override fun execute(descriptorProvider: NativeDescriptorProvider) {
        skieContext.module.file("TypeAliases") {
            addTypeAliasContainer(descriptorProvider)
            addBaseTypeAliasContainerTypeAlias()
        }
    }

    context(SwiftPoetScope)
    private fun FileSpec.Builder.addTypeAliasContainer(descriptorProvider: DescriptorProvider) {
        addType(
            TypeSpec.enumBuilder(DeclaredTypeName.qualifiedLocalTypeName(publicTypeAliasContainerName))
                .addModifiers(Modifier.PUBLIC)
                .addTypeAliases(descriptorProvider)
                .build()
        )
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addTypeAliases(descriptorProvider: DescriptorProvider): TypeSpec.Builder =
        this.apply {
            addClassTypeAliases(descriptorProvider)
            addFileTypeAliases(descriptorProvider)
        }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addClassTypeAliases(descriptorProvider: DescriptorProvider) {
        descriptorProvider.classDescriptors.forEach {
            addTypeAlias(it.swiftModel)
        }
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addFileTypeAliases(descriptorProvider: DescriptorProvider) {
        descriptorProvider.exportedFiles.forEach {
            addTypeAlias(it.swiftModel)
        }
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addTypeAlias(swiftModel: TypeSwiftModel) {
        addType(
            TypeAliasSpec.builder(
                name = swiftModel.stableFqName.removePrefix(KotlinTypeSwiftModel.StableFqNameNamespace),
                type = DeclaredTypeName.qualifiedLocalTypeName(swiftModel.fqName),
            )
                .addModifiers(Modifier.PUBLIC)
                .build()
        )
    }

    context(SwiftPoetScope)
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
