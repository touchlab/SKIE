@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor.cache

import co.touchlab.skie.kir.descriptor.cache.ExposedDescriptorTypeVisitor.TypeParameterRootScope
import co.touchlab.skie.kir.descriptor.cache.ExposedDescriptorTypeVisitor.TypeParameterScope
import co.touchlab.skie.shim.createObjCExportNamerImpl
import co.touchlab.skie.util.KotlinCompilerVersion
import co.touchlab.skie.util.SafeRecursionEngine
import co.touchlab.skie.util.current
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.descriptors.getPackageFragments
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.needCompanionObjectProperty
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.descriptors.isEnumClass
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.firstArgument
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter

// Based on ObjCExportTranslatorImpl and ObjCExportHeaderGenerator
internal class ExposedDescriptorsCache(
    private val mapper: CachedObjCExportMapper,
    builtIns: KotlinBuiltIns,
    objcGenerics: Boolean,
) {

    val exposedClasses: Set<ClassDescriptor> by ::mutableExposedClasses
    val exposedCategoryMembersByClass: Map<ClassDescriptor, List<CallableMemberDescriptor>> by ::mutableExposedCategoryMembersByClass
    val exposedCategoryMembers: Set<CallableMemberDescriptor> by ::mutableExposedCategoryMembers
    val exposedTopLevelMembersByFile: Map<SourceFile, List<CallableMemberDescriptor>> by ::mutableTopLevelMembersByFile
    val exposedTopLevelMembers: Set<CallableMemberDescriptor> by ::mutableExposedTopLevelMembers

    private val mutableExposedClasses = mutableSetOf<ClassDescriptor>()
    private val mutableExposedCategoryMembersByClass = mutableMapOf<ClassDescriptor, MutableList<CallableMemberDescriptor>>()
    private val mutableExposedCategoryMembers = mutableSetOf<CallableMemberDescriptor>()
    private val mutableTopLevelMembersByFile = mutableMapOf<SourceFile, MutableList<CallableMemberDescriptor>>()
    private val mutableExposedTopLevelMembers = mutableSetOf<CallableMemberDescriptor>()

    private val safeRecursionEngine = SafeRecursionEngine(::exposeClassRecursively)

    private val dummyNamer = createObjCExportNamerImpl(
        configuration = object : ObjCExportNamer.Configuration {
            override val objcGenerics: Boolean = objcGenerics
            override val topLevelNamePrefix: String = ""
            override fun getAdditionalPrefix(module: ModuleDescriptor): String? = null
        },
        builtIns = builtIns,
        mapper = mapper.kotlinMapper,
        local = true,
    )

    private val typeVisitor = ExposedDescriptorTypeVisitor(
        onExposedClassDescriptorVisited = { exposeClass(it) },
        mapper = mapper,
        objcGenerics = objcGenerics,
    )

    fun exposeModules(modules: Collection<ModuleDescriptor>) {
        modules.forEach(::exposeModule)
    }

    private fun exposeModule(module: ModuleDescriptor) {
        module.getPackageFragments().forEach(::exposePackageFragment)
    }

    private fun exposePackageFragment(packageFragment: PackageFragmentDescriptor) {
        val memberScope = packageFragment.getMemberScope()

        memberScope
            .getContributedDescriptors(kindFilter = DescriptorKindFilter.CALLABLES)
            .forEach {
                if (it is CallableMemberDescriptor) {
                    exposeGlobalMemberOrExtension(it)
                }
            }

        memberScope
            .getContributedDescriptors(kindFilter = DescriptorKindFilter.CLASSIFIERS)
            .forEach {
                if (it is ClassDescriptor) {
                    exposeClassIncludingNestedClasses(it)
                }
            }
    }

    fun exposeAnyMember(member: CallableMemberDescriptor) {
        val containingDeclaration = member.containingDeclaration

        if (containingDeclaration is ClassDescriptor) {
            val typeParameterScope = TypeParameterRootScope.deriveFor(containingDeclaration)

            exposeCallableMember(member, typeParameterScope)
            exposeClass(containingDeclaration)
        } else {
            exposeGlobalMemberOrExtension(member)
        }
    }

    private fun exposeGlobalMemberOrExtension(callableDeclaration: CallableMemberDescriptor) {
        if (!mapper.shouldBeExposed(callableDeclaration)) {
            return
        }

        val classDescriptor = mapper.getClassIfCategory(callableDeclaration)
        if (classDescriptor != null) {
            if (!mapper.isHiddenFromObjC(classDescriptor)) {
                mutableExposedCategoryMembersByClass.getOrPut(classDescriptor) { mutableListOf() } += callableDeclaration
                mutableExposedCategoryMembers += callableDeclaration

                exposeClass(classDescriptor)
                exposeCallableMember(callableDeclaration, TypeParameterRootScope)
            }
        } else if (mapper.isTopLevel(callableDeclaration)) {
            mutableTopLevelMembersByFile.getOrPut(callableDeclaration.findSourceFile()) { mutableListOf() } += callableDeclaration
            mutableExposedTopLevelMembers += callableDeclaration

            exposeCallableMember(callableDeclaration, TypeParameterRootScope)
        } else {
            error("Unsupported member: $callableDeclaration - not an extension or top-level declaration.")
        }
    }

    private fun exposeClassIncludingNestedClasses(classDescriptor: ClassDescriptor) {
        if (!mapper.shouldBeExposed(classDescriptor)) {
            return
        }

        exposeClass(classDescriptor)

        classDescriptor.unsubstitutedMemberScope
            .getContributedDescriptors(kindFilter = DescriptorKindFilter.CLASSIFIERS)
            .forEach {
                if (it is ClassDescriptor) {
                    exposeClassIncludingNestedClasses(it)
                }
            }
    }

    private fun exposeClass(classDescriptor: ClassDescriptor) {
        safeRecursionEngine.run(classDescriptor)
    }

    private fun exposeClassRecursively(classDescriptor: ClassDescriptor) {
        if (classDescriptor in exposedClasses) {
            return
        }

        if (!mapper.shouldBeExposed(classDescriptor)) {
            return
        }

        mutableExposedClasses.add(classDescriptor)

        if (!classDescriptor.kind.isInterface && classDescriptor.needCompanionObjectProperty(dummyNamer, mapper.kotlinMapper)) {
            classDescriptor.companionObjectDescriptor?.let(::exposeClass)
        }

        classDescriptor.defaultType.constructor.supertypes
            .map { it.constructor.declarationDescriptor }
            .filterIsInstance<ClassDescriptor>()
            .forEach(::exposeClass)

        typeVisitor.visitSuperClassTypeArguments(classDescriptor)

        exposeClassMembers(classDescriptor)
    }

    private fun exposeClass(fqName: String, origin: DeclarationDescriptor) {
        exposeClass(FqName(fqName), origin)
    }

    private fun exposeClass(fqName: FqName, origin: DeclarationDescriptor) {
        val classId = ClassId.topLevel(fqName)

        origin.module.findClassAcrossModuleDependencies(classId)?.let(::exposeClass)
    }

    private fun exposeClassMembers(classDescriptor: ClassDescriptor) {
        val typeParameterScope = TypeParameterRootScope.deriveFor(classDescriptor)

        classDescriptor.constructors.forEach {
            exposeFunction(it, typeParameterScope)
        }

        classDescriptor.unsubstitutedMemberScope
            .getContributedDescriptors(kindFilter = DescriptorKindFilter.CALLABLES)
            .forEach {
                if (it is CallableMemberDescriptor) {
                    exposeCallableMember(it, typeParameterScope)
                }
            }

        if (classDescriptor.kind.isEnumClass) {
            exposeClass("kotlin.Array", classDescriptor)

            if (KotlinCompilerVersion.current >= KotlinCompilerVersion.`1_9_0`) {
                exposeClass("kotlin.List", classDescriptor)
            }
        }
    }

    private fun exposeCallableMember(callableMember: CallableMemberDescriptor, typeParameterScope: TypeParameterScope) {
        when (callableMember) {
            is FunctionDescriptor -> exposeFunction(callableMember, typeParameterScope)
            is PropertyDescriptor -> if (mapper.isObjCProperty(callableMember)) {
                exposeProperty(callableMember, typeParameterScope)
            } else {
                exposeFunction((callableMember.getter ?: callableMember.setter)!!, typeParameterScope)
            }
            else -> error(callableMember)
        }
    }

    private fun exposeProperty(property: PropertyDescriptor, typeParameterScope: TypeParameterScope) {
        if (!mapper.shouldBeExposed(property)) {
            return
        }

        if (!property.isExplicitlyInHeader()) {
            return
        }

        mapper.getBaseProperties(property).forEach { baseProperty ->
            val getterBridge = mapper.bridgeMethod(baseProperty.getter!!)

            typeVisitor.visitReturnType(getterBridge.returnBridge, property.getter!!, typeParameterScope)
        }
    }

    private fun exposeFunction(
        function: FunctionDescriptor,
        typeParameterScope: TypeParameterScope,
    ) {
        if (!mapper.shouldBeExposed(function)) {
            return
        }

        exposeThrownClasses(function)

        if (!function.isExplicitlyInHeader()) {
            return
        }

        mapper.getBaseMethods(function).forEach { baseMethod ->
            val baseMethodBridge = mapper.bridgeMethod(baseMethod)

            typeVisitor.visitReturnType(baseMethodBridge.returnBridge, function, typeParameterScope)

            typeVisitor.visitParameterTypes(baseMethodBridge, function, typeParameterScope)
        }
    }

    private fun exposeThrownClasses(method: FunctionDescriptor) {
        if (method.isSuspend && method.overriddenDescriptors.isEmpty()) {
            exposeClass(KonanFqNames.cancellationException, method)
        }

        (method.annotations.findAnnotation(KonanFqNames.throws)?.firstArgument() as? ArrayValue?)
            ?.value
            ?.filterIsInstance<KClassValue>()
            ?.mapNotNull {
                when (val value = it.value) {
                    is KClassValue.Value.NormalClass -> value.classId
                    is KClassValue.Value.LocalClass -> null
                }
            }
            ?.mapNotNull { method.module.findClassAcrossModuleDependencies(it) }
            ?.forEach {
                exposeClass(it)
            }
    }

    private fun CallableMemberDescriptor.isExplicitlyInHeader(): Boolean {
        val ownerClass = containingDeclaration as? ClassDescriptor ?: return true

        if (!this.kind.isReal) {
            return false
        }

        if (ownerClass.kind.isInterface) {
            return when (this) {
                is FunctionDescriptor -> mapper.isBaseMethod(this)
                is PropertyDescriptor -> mapper.isBaseProperty(this)
                else -> error(this)
            }
        }

        return true
    }
}
