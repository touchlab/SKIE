@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model

import co.touchlab.skie.api.model.function.ActualKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.property.extension.ActualKotlinConvertedPropertySwiftModel
import co.touchlab.skie.api.model.property.regular.ActualKotlinRegularPropertySwiftModel
import co.touchlab.skie.api.model.type.classes.ActualKotlinClassSwiftModel
import co.touchlab.skie.api.model.type.files.ActualKotlinFileSwiftModel
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import co.touchlab.skie.util.getClassSwiftName
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.KotlinType

class DefaultSwiftModelScope(
    private val namer: ObjCExportNamer,
    descriptorProvider: DescriptorProvider,
) : MutableSwiftModelScope {

    private val storageManager = LockBasedStorageManager("DefaultSwiftModelScope")

    private val functionModelCache =
        storageManager.createMemoizedFunction<FunctionDescriptor, MutableKotlinFunctionSwiftModel> { functionDescriptor ->
            if (!namer.mapper.isBaseMethod(functionDescriptor)) {
                throw IllegalArgumentException(
                    "Overriding functions are not supporting. Obtain model of the base overridden function instead: $functionDescriptor"
                )
            }

            ActualKotlinFunctionSwiftModel(functionDescriptor, functionDescriptor.receiverModel, namer)
        }

    private val CallableMemberDescriptor.receiverModel: MutableKotlinTypeSwiftModel
        get() {
            val categoryClass = namer.mapper.getClassIfCategory(this)
            val containingDeclaration = this.containingDeclaration

            return when {
                categoryClass != null -> categoryClass.swiftModel
                this is PropertyAccessorDescriptor -> correspondingProperty.receiverModel
                containingDeclaration is ClassDescriptor -> containingDeclaration.swiftModel
                containingDeclaration is PackageFragmentDescriptor -> this.findSourceFile().swiftModel
                else -> error("Unsupported containing declaration for $this")
            }
        }

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel
        get() = functionModelCache(this)

    override val ValueParameterDescriptor.swiftModel: MutableKotlinParameterSwiftModel
        get() = (this.containingDeclaration as FunctionDescriptor).swiftModel.parameters.first { it.descriptor == this }

    private val regularPropertyModelCache =
        storageManager.createMemoizedFunction<PropertyDescriptor, MutableKotlinRegularPropertySwiftModel> { propertyDescriptor ->
            if (!namer.mapper.isBaseProperty(propertyDescriptor)) {
                throw IllegalArgumentException(
                    "Overriding properties are not supporting. Obtain model of the base overridden property instead: $propertyDescriptor"
                )
            }
            if (!namer.mapper.isObjCProperty(propertyDescriptor)) {
                throw IllegalArgumentException("Converted properties must be handled separately from regular ones: $propertyDescriptor")
            }

            ActualKotlinRegularPropertySwiftModel(propertyDescriptor, propertyDescriptor.receiverModel, namer)
        }

    override val PropertyDescriptor.regularPropertySwiftModel: MutableKotlinRegularPropertySwiftModel
        get() = regularPropertyModelCache(this)

    private val interfaceExtensionPropertyModelCache =
        storageManager.createMemoizedFunction<PropertyDescriptor, MutableKotlinConvertedPropertySwiftModel> { propertyDescriptor ->
            if (!namer.mapper.isBaseProperty(propertyDescriptor)) {
                throw IllegalArgumentException(
                    "Overriding properties are not supporting. Obtain model of the base overridden property instead: $propertyDescriptor"
                )
            }
            if (namer.mapper.isObjCProperty(propertyDescriptor)) {
                throw IllegalArgumentException("Property $propertyDescriptor is a regular property not a converted one.")
            }

            ActualKotlinConvertedPropertySwiftModel(
                descriptor = propertyDescriptor,
                getter = propertyDescriptor.getter?.swiftModel ?: error("Property does not have a getter: $propertyDescriptor"),
                setter = propertyDescriptor.setter?.swiftModel,
            )
        }

    override val PropertyDescriptor.convertedPropertySwiftModel: MutableKotlinConvertedPropertySwiftModel
        get() = interfaceExtensionPropertyModelCache(this)

    override val PropertyDescriptor.swiftModel: KotlinPropertySwiftModel
        get() = if (namer.mapper.isObjCProperty(this)) regularPropertySwiftModel else convertedPropertySwiftModel

    override val CallableMemberDescriptor.swiftModel: KotlinCallableMemberSwiftModel
        get() = when (this) {
            is FunctionDescriptor -> this.swiftModel
            is PropertyDescriptor -> this.swiftModel
            else -> error("Unsupported callable member: $this")
        }

    private val classModelCache =
        storageManager.createMemoizedFunction<ClassDescriptor, MutableKotlinClassSwiftModel> { classDescriptor ->
            val fullName = namer.getClassSwiftName(classDescriptor)

            val containingType = if (fullName.contains(".")) {
                val containingClassName = fullName.substringBefore(".")

                classDescriptor.containingClassNamed(containingClassName).swiftModel
            } else {
                null
            }

            ActualKotlinClassSwiftModel(classDescriptor, containingType, namer)
        }

    private fun ClassDescriptor.containingClassNamed(name: String): ClassDescriptor {
        val containingClass = this.containingDeclaration as ClassDescriptor

        val containingClassName = namer.getClassSwiftName(containingClass)

        return if (containingClassName == name) containingClass else containingClass.containingClassNamed(name)
    }

    override val ClassDescriptor.swiftModel: MutableKotlinClassSwiftModel
        get() = classModelCache(this)

    private val fileModelCache = storageManager.createMemoizedFunction<SourceFile, MutableKotlinTypeSwiftModel> { file ->
        ActualKotlinFileSwiftModel(file, namer, descriptorProvider)
    }

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
        get() = fileModelCache(this)

    override val KotlinType.isBridged: Boolean
        get() = when (val descriptor = constructor.declarationDescriptor) {
            is ClassDescriptor -> descriptor.swiftModel.bridge != null
            is TypeAliasDescriptor -> descriptor.expandedType.isBridged
            else -> false
        }
}
