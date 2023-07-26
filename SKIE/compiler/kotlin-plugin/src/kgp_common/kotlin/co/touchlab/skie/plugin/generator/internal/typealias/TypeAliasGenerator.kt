package co.touchlab.skie.plugin.generator.internal.`typealias`

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec

internal class TypeAliasGenerator(
    private val skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val areTypeAliasesExported = SkieFeature.FqNames in skieContext.skieConfiguration.enabledFeatures

    private val baseTypeAliasContainerName = "Skie"

    override fun runObjcPhase() {
        skieContext.module.file("SkieTypeAliases") {
            addTypeAliasContainer(descriptorProvider)
            addBaseTypeAliasContainerTypeAlias()
        }
    }

    context(SwiftModelScope)
    private fun FileSpec.Builder.addTypeAliasContainer(descriptorProvider: DescriptorProvider) {
        addType(
            TypeSpec.enumBuilder(KotlinTypeSwiftModel.StableFqNameNamespace.toSwiftPoetName())
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
        descriptorProvider.exposedClasses.forEach {
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
    private fun TypeSpec.Builder.addTypeAlias(swiftModel: KotlinTypeSwiftModel) {
        addTypeAliasIfLocalClass(swiftModel.nonBridgedDeclaration)
        swiftModel.bridge?.declaration?.let { addTypeAliasIfLocalClass(it) }
    }

    private fun TypeSpec.Builder.addTypeAliasIfLocalClass(declaration: SwiftIrExtensibleDeclaration) {
        if (declaration is SwiftIrExtensibleDeclaration.Local) {
            addTypeAlias(declaration)
        }
    }

    private fun TypeSpec.Builder.addTypeAlias(localClass: SwiftIrExtensibleDeclaration.Local) {
        addType(
            TypeAliasSpec.builder(
                name = localClass.typealiasName,
                type = localClass.publicName.toSwiftPoetName().let {
                    DeclaredTypeName.qualifiedTypeName("${skieContext.frameworkLayout.moduleName}.${it.simpleNames.joinToString(".")}")
                },
            )
                .addModifiers(Modifier.PUBLIC)
                .build()
        )
    }

    context(SwiftModelScope)
    private fun FileSpec.Builder.addBaseTypeAliasContainerTypeAlias() {
        val builder = TypeAliasSpec.builder(
            name = baseTypeAliasContainerName,
            type = KotlinTypeSwiftModel.StableFqNameNamespace.toSwiftPoetName(),
        )

        if (areTypeAliasesExported) {
            builder.addModifiers(Modifier.PUBLIC)
        }

        addType(builder.build())
    }
}
