package co.touchlab.skie.plugin.api.sir

import co.touchlab.skie.api.model.type.translation.SwiftTypeTranslator
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.sir.builtin.SirBuiltins
import co.touchlab.skie.plugin.api.sir.element.SirClass
import co.touchlab.skie.plugin.api.sir.element.SirTypeAlias
import co.touchlab.skie.plugin.api.sir.element.SirTypeParameter
import co.touchlab.skie.plugin.api.sir.type.DeclaredSirType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isInterface

// WIP Refactor
class KotlinSirClassFactory(
    private val sirProvider: SirProvider,
    private val translator: SwiftTypeTranslator,
    private val namespaceProvider: SirNamespaceProvider,
    private val namer: ObjCExportNamer,
    private val descriptorProvider: DescriptorProvider,
) {

    private val kotlinSirClassCache = mutableMapOf<ClassDescriptor, SirClass>()

    private val classDescriptorToFqNameMap = mutableMapOf<ClassDescriptor, SirFqName>()

    private val fqNameToClassDescriptorMap = mutableMapOf<SirFqName, ClassDescriptor>()

    private val superTypesInitializationBlocks = mutableListOf<SwiftModelScope.() -> Unit>()

    val sirBuiltins: SirBuiltins
        get() = sirProvider.sirBuiltins

    context(SwiftModelScope)
    internal fun finishInitialization() {
        superTypesInitializationBlocks.forEach { it(this@SwiftModelScope) }
    }

    fun getKotlinSirClass(classDescriptor: ClassDescriptor): SirClass =
        kotlinSirClassCache.getOrPut(classDescriptor) {
            val fqName = classDescriptor.sirFqName

            val sirClass = SirClass(
                simpleName = fqName.simpleName,
                parent = fqName.parent?.let { getKotlinSirClass(it.classDescriptor) } ?: sirBuiltins.Kotlin.module,
                kind = if (classDescriptor.kind.isInterface) SirClass.Kind.Protocol else SirClass.Kind.Class,
            )

            if (sirClass.kind != SirClass.Kind.Protocol) {
                classDescriptor.typeConstructor.parameters.forEach { typeParameter ->
                    SirTypeParameter(
                        name = typeParameter.name.asString(),
                        parent = sirClass,
                        bounds = listOf(sirBuiltins.Swift.AnyObject.defaultType),
                    )
                }
            }

            superTypesInitializationBlocks.add {
                sirClass.initializeSuperTypes(classDescriptor)
            }

            val namespace = namespaceProvider.getOrCreateNamespace(classDescriptor)
            createKotlinTypeAlias(sirClass, namespace)

            sirClass
        }

    private fun createKotlinTypeAlias(sirClass: SirClass, namespace: SirClass) {
        sirClass.internalTypeAlias = SirTypeAlias(
            simpleName = "__Kotlin",
            parent = namespace,
        ) {
            sirClass.defaultType.also { it.useInternalName = false }
        }
    }

    context(SwiftModelScope)
    private fun SirClass.initializeSuperTypes(classDescriptor: ClassDescriptor) {
        val genericExportScope = SwiftGenericExportScope.Class(classDescriptor, typeParameters)

        val directlyInheritsAny = classDescriptor.defaultType
            .constructor
            .supertypes
            .any { KotlinBuiltIns.isAnyOrNullableAny(it) }

        if (directlyInheritsAny) {
            superTypes.add(sirBuiltins.Kotlin.Base.defaultType)
        }

        val superTypesWithoutAny = classDescriptor.defaultType
            .constructor
            .supertypes
            .filter { !KotlinBuiltIns.isAnyOrNullableAny(it) }
            .mapNotNull {
                translator.mapReferenceType(it, SwiftExportScope(genericExportScope), FlowMappingStrategy.TypeArgumentsOnly)
            }
            .filterIsInstance<DeclaredSirType>()

        superTypes.addAll(superTypesWithoutAny)
    }

    fun createKotlinSirClass(sourceFile: SourceFile): SirClass {
        val sirClass = SirClass(
            simpleName = namer.getFileClassName(sourceFile).swiftName,
            parent = sirBuiltins.Kotlin.module,
            kind = SirClass.Kind.Class,
            superTypes = listOf(sirBuiltins.Kotlin.Base.defaultType),
        )

        val namespace = namespaceProvider.getOrCreateNamespace(sourceFile)
        createKotlinTypeAlias(sirClass, namespace)

        return sirClass
    }

    private val ClassDescriptor.sirFqName: SirFqName
        get() = classDescriptorToFqNameMap.getOrPut(this) {
            val fullName = namer.getClassOrProtocolName(this.original).swiftName

            val fqName = if (fullName.contains(".")) getNestedClassSwiftName(this, fullName) else getTopLevelClassSwiftName(fullName)

            fqName.also { fqNameToClassDescriptorMap[it] = this }
        }

    private val SirFqName.classDescriptor: ClassDescriptor
        get() = fqNameToClassDescriptorMap.getOrPut(this) {
            descriptorProvider.exposedClasses.firstOrNull { it.sirFqName == this }
                ?: throw IllegalStateException("ClassDescriptor for $this not found.")
        }

    private fun getNestedClassSwiftName(descriptor: ClassDescriptor, fullName: String): SirFqName {
        val containingClassSimpleName = fullName.substringBefore(".")
        val currentClassSimpleName = fullName.substringAfter(".")

        val containingClass = descriptor.getContainingClassNamed(containingClassSimpleName)

        return if (containingClass in descriptorProvider.exposedClasses) {
            containingClass.sirFqName.nested(currentClassSimpleName)
        } else {
            val mergedName = containingClassSimpleName + currentClassSimpleName.replaceFirstChar(Char::uppercaseChar)

            getTopLevelClassSwiftName(mergedName)
        }
    }

    private fun getTopLevelClassSwiftName(fullName: String): SirFqName =
        SirFqName(
            module = sirBuiltins.Kotlin.module,
            simpleName = fullName,
        )

    private fun ClassDescriptor.getContainingClassNamed(name: String): ClassDescriptor {
        val containingClass = this.containingDeclaration as ClassDescriptor

        val containingClassName = namer.getClassOrProtocolName(containingClass.original).swiftName

        return if (containingClassName == name) containingClass else containingClass.getContainingClassNamed(name)
    }
}

