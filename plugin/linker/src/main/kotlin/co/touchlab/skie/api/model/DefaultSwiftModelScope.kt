@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model

import co.touchlab.skie.api.model.function.ActualKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.property.ActualKotlinPropertySwiftModel
import co.touchlab.skie.api.model.type.classes.ActualKotlinClassSwiftModel
import co.touchlab.skie.api.model.type.files.ActualKotlinFileSwiftModel
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.property.MutableKotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import co.touchlab.skie.util.getClassSwiftName
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.storage.LockBasedStorageManager

class DefaultSwiftModelScope(
    private val namer: ObjCExportNamer,
    descriptorProvider: DescriptorProvider,
) : MutableSwiftModelScope {

    private val storageManager = LockBasedStorageManager("DefaultSwiftModelScope")

    private val functionModelCache =
        storageManager.createMemoizedFunction<FunctionDescriptor, MutableKotlinFunctionSwiftModel> { functionDescriptor ->
            if (!namer.mapper.isBaseMethod(functionDescriptor)) {
                throw IllegalArgumentException("Overriding functions are not supporting. Obtain model of the base overridden function instead.")
            }

            ActualKotlinFunctionSwiftModel(functionDescriptor, functionDescriptor.receiverModel, namer)
        }

    private val CallableMemberDescriptor.receiverModel: MutableKotlinTypeSwiftModel
        get() {
            val categoryClass = namer.mapper.getClassIfCategory(this)
            val containingDeclaration = this.containingDeclaration

            return when {
                categoryClass != null -> categoryClass.swiftModel
                containingDeclaration is ClassDescriptor -> containingDeclaration.swiftModel
                containingDeclaration is PackageFragmentDescriptor -> this.findSourceFile().swiftModel
                else -> error("Unsupported containing declaration for $this")
            }
        }

    override val FunctionDescriptor.swiftModel: MutableKotlinFunctionSwiftModel
        get() = functionModelCache(this)

    private val propertyModelCache =
        storageManager.createMemoizedFunction<PropertyDescriptor, MutableKotlinPropertySwiftModel> { propertyDescriptor ->
            if (!namer.mapper.isBaseProperty(propertyDescriptor)) {
                throw IllegalArgumentException("Overriding properties are not supporting. Obtain model of the base overridden property instead.")
            }

            ActualKotlinPropertySwiftModel(propertyDescriptor, propertyDescriptor.receiverModel, namer)
        }

    override val PropertyDescriptor.swiftModel: MutableKotlinPropertySwiftModel
        get() = propertyModelCache(this)

    private val classModelCache =
        storageManager.createMemoizedFunction<ClassDescriptor, MutableKotlinTypeSwiftModel> { classDescriptor ->
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

    override val ClassDescriptor.swiftModel: MutableKotlinTypeSwiftModel
        get() = classModelCache(this)

    private val fileModelCache = storageManager.createMemoizedFunction<SourceFile, MutableKotlinTypeSwiftModel> { file ->
        ActualKotlinFileSwiftModel(file, namer, descriptorProvider)
    }

    override val SourceFile.swiftModel: MutableKotlinTypeSwiftModel
        get() = fileModelCache(this)
}
