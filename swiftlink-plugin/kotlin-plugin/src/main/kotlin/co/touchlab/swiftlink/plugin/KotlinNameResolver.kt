package co.touchlab.swiftlink.plugin

import co.touchlab.swiftpack.spec.KotlinFileReference
import co.touchlab.swiftpack.spec.KotlinFunctionReference
import co.touchlab.swiftpack.spec.KotlinPackageReference
import co.touchlab.swiftpack.spec.KotlinPropertyReference
import co.touchlab.swiftpack.spec.KotlinTypeReference
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class KotlinNameResolver(
    private val context: CommonBackendContext,
) {
    fun resolvePackageView(reference: KotlinPackageReference): PackageViewDescriptor {
        val packageName = if(reference.packageName.isNotBlank()) FqName(reference.packageName) else FqName.ROOT
        return context.ir.irModule.descriptor.getPackage(packageName)
    }

    fun findFile(reference: KotlinFileReference): FileInPackage? {
        val packageDescriptor = resolvePackageView(reference.packageReference)
        val fileName = reference.fileName + ".kt"

        return packageDescriptor.memberScope.getContributedDescriptors()
            .asSequence()
            .filterIsInstance<CallableMemberDescriptor>()
            .map { it.findSourceFile() }
            .firstOrNull {
                it.name == fileName
            }
            ?.let {
                FileInPackage(it, reference.packageReference.fqName)
            }
    }

    fun resolveProperty(reference: KotlinPropertyReference): PropertyDescriptor {
        return checkNotNull(findProperty(reference)) {
            "Could not find property $reference"
        }
    }

    fun findProperty(reference: KotlinPropertyReference): PropertyDescriptor? {
        val parent = reference.parent
        val memberScope = when (parent) {
            is KotlinPackageReference -> resolvePackageView(parent).memberScope
            is KotlinTypeReference -> resolveClass(parent).unsubstitutedMemberScope
        }

        return memberScope.getContributedVariables(Name.identifier(reference.propertyName), NoLookupLocation.FROM_BACKEND).singleOrNull()
    }

    fun resolveFunction(reference: KotlinFunctionReference): FunctionDescriptor {
        return checkNotNull(findFunction(reference)) {
            "Could not find function $reference"
        }
    }

    fun findFunction(reference: KotlinFunctionReference): FunctionDescriptor? {
        val parent = reference.parent
        val memberScope = when (parent) {
            is KotlinPackageReference -> resolvePackageView(parent).memberScope
            is KotlinTypeReference -> resolveClass(parent).unsubstitutedMemberScope
        }

        // TODO: Find one with correct parameter types
        return memberScope.getContributedFunctions(Name.identifier(reference.functionName), NoLookupLocation.FROM_BACKEND).singleOrNull {
            it.valueParameters.size == reference.parameterTypes.size && it.valueParameters.zip(reference.parameterTypes).all { (param, type) ->
                param.type.constructor.declarationDescriptor?.fqNameSafe == type.fqName
            }
        }
    }

    fun resolveClass(reference: KotlinTypeReference): ClassDescriptor {
        return checkNotNull(findClass(reference)) {
            "Couldn't resolve class descriptor for $reference"
        }
    }

    fun findClass(reference: KotlinTypeReference): ClassDescriptor? {
        return context.ir.irModule.descriptor.resolveClassByFqName(
            reference.fqName,
            NoLookupLocation.FROM_BACKEND
        )
    }

    private val KotlinPackageReference.fqName: FqName
        get() = if(packageName.isNotBlank()) FqName(packageName) else FqName.ROOT
}
