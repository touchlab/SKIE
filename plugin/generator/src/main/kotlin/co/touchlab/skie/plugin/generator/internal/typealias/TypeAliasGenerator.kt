package co.touchlab.skie.plugin.generator.internal.`typealias`

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.SwiftPoetScope
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.api.util.typeAliasName
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class TypeAliasGenerator(
    private val skieContext: SkieContext,
    configuration: Configuration,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    private val areTypeAliasesExported = SkieFeature.FqNames in configuration.enabledFeatures

    private val baseTypeAliasContainerName = "Skie"

    private val typeAliasContainerName = if (areTypeAliasesExported) baseTypeAliasContainerName else "__$baseTypeAliasContainerName"

    override fun execute(descriptorProvider: DescriptorProvider) {
        skieContext.module.file("TypeAliases") {
            addTypeAliasContainer(descriptorProvider)
            addTypeAliasContainerInternalTypeAlias()
        }
    }

    context(SwiftPoetScope)
        private fun FileSpec.Builder.addTypeAliasContainer(descriptorProvider: DescriptorProvider) {
        addType(
            TypeSpec.enumBuilder(DeclaredTypeName.qualifiedLocalTypeName(typeAliasContainerName))
                .addModifiers(Modifier.PUBLIC)
                .addTypeAliases(descriptorProvider)
                .build()
        )
    }

    context(SwiftPoetScope)
        private fun TypeSpec.Builder.addTypeAliases(descriptorProvider: DescriptorProvider): TypeSpec.Builder =
        this.apply {
            descriptorProvider.classDescriptors.forEach { classDescriptor ->
                addTypeAlias(classDescriptor)
            }
        }

    context(SwiftPoetScope)
        private fun TypeSpec.Builder.addTypeAlias(classDescriptor: ClassDescriptor) {
        addType(
            TypeAliasSpec.builder(
                name = classDescriptor.typeAliasName,
                type = classDescriptor.spec,
            )
                .addModifiers(Modifier.PUBLIC)
                .build()
        )
    }

    context(SwiftPoetScope)
        private fun FileSpec.Builder.addTypeAliasContainerInternalTypeAlias() {
        if (areTypeAliasesExported) {
            return
        }

        addType(
            TypeAliasSpec.builder(
                name = baseTypeAliasContainerName,
                type = DeclaredTypeName.qualifiedLocalTypeName(typeAliasContainerName),
            )
                .build()
        )
    }
}
