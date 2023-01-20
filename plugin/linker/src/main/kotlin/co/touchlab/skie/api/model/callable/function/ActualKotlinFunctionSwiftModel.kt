package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.api.model.callable.getReceiverSwiftModel
import co.touchlab.skie.api.model.callable.parameter.ActualKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor

internal class ActualKotlinFunctionSwiftModel(
    override val descriptor: FunctionDescriptor,
    override val allBoundedSwiftModels: List<MutableKotlinCallableMemberSwiftModel>,
    core: KotlinFunctionSwiftModelCore,
    namer: ObjCExportNamer,
    swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinFunctionSwiftModel {

    override var identifier: String by core::identifier

    override val parameters: List<MutableKotlinParameterSwiftModel> = core.getParameterCoresWithDescriptors(descriptor).map {
        ActualKotlinParameterSwiftModel(it.first, it.second)
    }

    override var visibility: SwiftModelVisibility by core::visibility

    override val receiver: MutableKotlinTypeSwiftModel by lazy {
        with(swiftModelScope) {
            descriptor.getReceiverSwiftModel(namer)
        }
    }

    override val objCSelector: String by core::objCSelector

    override val kind: KotlinFunctionSwiftModel.Kind
        get() = when (descriptor) {
            is ConstructorDescriptor -> KotlinFunctionSwiftModel.Kind.Constructor
            is PropertyGetterDescriptor -> KotlinFunctionSwiftModel.Kind.ConvertedGetter
            is PropertySetterDescriptor -> KotlinFunctionSwiftModel.Kind.ConvertedSetter
            else -> KotlinFunctionSwiftModel.Kind.SimpleFunction
        }

    override val original: KotlinFunctionSwiftModel = OriginalKotlinFunctionSwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility || parameters.any { it.isChanged }

    override val returnType: TypeSwiftModel
        get() = TODO()
}

