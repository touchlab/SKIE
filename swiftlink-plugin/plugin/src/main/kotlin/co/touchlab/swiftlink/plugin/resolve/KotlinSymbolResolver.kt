package co.touchlab.swiftlink.plugin.resolve

import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeParameterReference
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

@OptIn(ObsoleteDescriptorBasedAPI::class)
class KotlinSymbolResolver(
    private val context: CommonBackendContext,
    private val symbolRegistry: KotlinSymbolRegistry,
) {
    private val symbolTable: ReferenceSymbolTable
        get() = context.ir.symbols.externalSymbolTable

    private val moduleDescriptor: ModuleDescriptor
        get() = context.ir.irModule.descriptor

    fun resolveProperty(propertyId: KotlinPropertyReference.Id): PropertyDescriptor {
        return resolveProperty(symbolRegistry[propertyId])
    }

    fun resolveProperty(reference: KotlinPropertyReference): PropertyDescriptor {
        val descriptor = symbolTable.referenceProperty(sig = reference.signature).descriptor
        val memberScope = if (descriptor.dispatchReceiverParameter != null) {
            moduleDescriptor.resolveClassByFqName(descriptor.containingDeclaration.fqNameSafe, NoLookupLocation.FROM_BACKEND)
                ?.unsubstitutedMemberScope ?: return descriptor
        } else {
            moduleDescriptor.getPackage(descriptor.findPackage().fqName).memberScope
        }

        return memberScope.getContributedVariables(descriptor.name, NoLookupLocation.FROM_BACKEND).single()
    }

    fun resolveFunction(functionId: KotlinFunctionReference.Id): FunctionDescriptor {
        return resolveFunction(symbolRegistry[functionId])
    }

    fun resolveFunction(reference: KotlinFunctionReference): FunctionDescriptor {
        val descriptor = symbolTable.referenceSimpleFunction(sig = reference.signature).descriptor
        val memberScope = if (descriptor.dispatchReceiverParameter != null) {
            moduleDescriptor.resolveClassByFqName(descriptor.containingDeclaration.fqNameSafe, NoLookupLocation.FROM_BACKEND)
                ?.unsubstitutedMemberScope ?: return descriptor
        } else {
            moduleDescriptor.getPackage(descriptor.findPackage().fqName).memberScope
        }

        return memberScope.getContributedFunctions(descriptor.name, NoLookupLocation.FROM_BACKEND).single {
            it.valueParameters.size == descriptor.valueParameters.size && it.valueParameters.zip(descriptor.valueParameters).all { (a, b) ->
                a.type == b.type
            }
        }
    }

    fun resolveClass(classId: KotlinClassReference.Id): ClassDescriptor {
        return resolveClass(symbolRegistry[classId])
    }

    fun resolveClass(reference: KotlinClassReference): ClassDescriptor {
        val descriptor = symbolTable.referenceClass(sig = reference.signature).descriptor

        return moduleDescriptor.resolveClassByFqName(descriptor.fqNameSafe, NoLookupLocation.FROM_BACKEND) ?: descriptor
    }

    fun resolveTypeParameter(reference: KotlinTypeParameterReference.Id): TypeParameterDescriptor {
        return resolveTypeParameter(symbolRegistry[reference])
    }

    fun resolveTypeParameter(reference: KotlinTypeParameterReference): TypeParameterDescriptor {
        return symbolTable.referenceTypeParameter(sig = reference.signature).descriptor
    }

    fun resolveEnumEntry(reference: KotlinEnumEntryReference.Id): ClassDescriptor {
        return resolveEnumEntry(symbolRegistry[reference])
    }

    fun resolveEnumEntry(reference: KotlinEnumEntryReference): ClassDescriptor {
        val descriptor = symbolTable.referenceEnumEntry(sig = reference.signature).descriptor

        return moduleDescriptor.resolveClassByFqName(descriptor.fqNameSafe, NoLookupLocation.FROM_BACKEND) ?: descriptor
    }
}
