package co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl

import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationTemplate
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.FunctionBuilder
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.Namespace
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.SecondaryConstructorBuilder
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.namespace.DeserializedClassNamespace
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.namespace.DeserializedPackageNamespace
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.namespace.NewFileNamespace
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.template.FunctionTemplate
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.template.SecondaryConstructorTemplate
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ContextReflector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

internal class DeclarationBuilderImpl(
    context: CommonBackendContext,
) : DeclarationBuilder {

    private val symbolTable = context.reflectedBy<ContextReflector>().symbolTable

    private lateinit var mainIrModuleFragment: IrModuleFragment

    private val newFileNamespaceFactory = NewFileNamespace.Factory(context, lazy { mainIrModuleFragment })

    private val newFileNamespacesByName = mutableMapOf<String, NewFileNamespace>()
    private val classNamespacesByDescriptor = mutableMapOf<ClassDescriptor, DeserializedClassNamespace>()
    private val packageNamespacesByDescriptor = mutableMapOf<PackageFragmentDescriptor, DeserializedPackageNamespace>()

    private val allNamespaces: List<Namespace<*>>
        get() = listOf(
            newFileNamespacesByName,
            classNamespacesByDescriptor,
            packageNamespacesByDescriptor,
        ).flatMap { it.values }

    override fun getCustomNamespace(name: String): Namespace<PackageFragmentDescriptor> =
        newFileNamespacesByName.getOrPut(name) {
            newFileNamespaceFactory.create(name)
        }

    override fun getClassNamespace(classDescriptor: ClassDescriptor): Namespace<ClassDescriptor> =
        classNamespacesByDescriptor.getOrPut(classDescriptor) {
            require(classDescriptor is DeserializedClassDescriptor) {
                "Only DeserializedClassDescriptor is currently supported. Was: $classDescriptor"
            }

            DeserializedClassNamespace(classDescriptor)
        }

    override fun getPackageNamespace(existingMember: FunctionDescriptor): Namespace<PackageFragmentDescriptor> {
        val packageFragment = existingMember.containingDeclaration as? PackageFragmentDescriptor
            ?: throw IllegalArgumentException("existingMember must be a direct package member.")

        return packageNamespacesByDescriptor.getOrPut(packageFragment) {
            DeserializedPackageNamespace(existingMember)
        }
    }

    override fun createFunction(
        name: Name,
        namespace: Namespace<*>,
        annotations: Annotations,
        builder: FunctionBuilder.() -> Unit,
    ): FunctionDescriptor =
        create(namespace) { FunctionTemplate(name, namespace, annotations, builder) }

    override fun createSecondaryConstructor(
        name: Name,
        namespace: Namespace<ClassDescriptor>,
        annotations: Annotations,
        builder: SecondaryConstructorBuilder.() -> Unit,
    ): ClassConstructorDescriptor =
        create(namespace) { SecondaryConstructorTemplate(name, namespace, annotations, builder) }

    private fun <D : DeclarationDescriptor> create(
        namespace: Namespace<*>,
        templateBuilder: () -> DeclarationTemplate<D>,
    ): D {
        val declarationTemplate = templateBuilder()

        namespace.addTemplate(declarationTemplate, symbolTable)

        return declarationTemplate.descriptor
    }

    fun generateIr(mairIrModuleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        this.mainIrModuleFragment = mairIrModuleFragment

        allNamespaces.forEach {
            it.generateIr(pluginContext, symbolTable)
        }
    }
}
