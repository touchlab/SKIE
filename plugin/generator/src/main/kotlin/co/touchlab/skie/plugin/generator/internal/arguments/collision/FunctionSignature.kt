package co.touchlab.skie.plugin.generator.internal.arguments.collision

import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isInterface

// This implementation is significantly simplified and does not correctly handle all cases - especially generics and inheritance.
internal sealed class FunctionSignature {

    abstract val overloadResolutionSelector: String

    abstract fun copy(valueParameters: List<ParameterDescriptor>): FunctionSignature

    class GlobalFunction(
        private val file: String?,
        private val name: Name,
        private val valueParameters: List<ParameterDescriptor>,
    ) : FunctionSignature() {

        override val overloadResolutionSelector: String
            get() = "$file#${name.identifier}(${valueParameters.joinNamesWithTypes()})"

        override fun copy(valueParameters: List<ParameterDescriptor>): FunctionSignature =
            GlobalFunction(file, name, valueParameters)
    }

    class ClassExtension(
        private val name: Name,
        private val extensionReceiver: ReceiverParameterDescriptor,
        private val valueParameters: List<ParameterDescriptor>,
    ) : FunctionSignature() {

        override val overloadResolutionSelector: String
            get() = "${extensionReceiver.type.upperBoundFqName}.${name.identifier}(${valueParameters.joinNamesWithTypes()})"

        override fun copy(valueParameters: List<ParameterDescriptor>): FunctionSignature =
            ClassExtension(name, extensionReceiver, valueParameters)
    }

    class ClassMethod(
        private val name: Name,
        private val dispatchReceiver: ReceiverParameterDescriptor,
        private val extensionReceiver: ReceiverParameterDescriptor?,
        private val valueParameters: List<ParameterDescriptor>,
    ) : FunctionSignature() {

        private val extensionReceiverSelectorFragment = extensionReceiver?.type?.upperBoundFqName?.let { "$it@" } ?: ""

        override val overloadResolutionSelector: String
            get() = "${dispatchReceiver.type.upperBoundFqName}.${extensionReceiverSelectorFragment}${name.identifier}(${valueParameters.joinNamesWithTypes()})"

        override fun copy(valueParameters: List<ParameterDescriptor>): FunctionSignature =
            ClassMethod(name, dispatchReceiver, extensionReceiver, valueParameters)
    }

    class Constructor(
        private val constructedClass: ClassDescriptor,
        private val valueParameters: List<ParameterDescriptor>,
    ) : FunctionSignature() {

        override val overloadResolutionSelector: String
            get() = "${constructedClass.fqNameSafe}.<init>(${valueParameters.joinNamesWithTypes()})"

        override fun copy(valueParameters: List<ParameterDescriptor>): FunctionSignature =
            Constructor(constructedClass, valueParameters)
    }

    class InterfaceExtension(
        private val file: String?,
        private val name: Name,
        private val extensionReceiver: ReceiverParameterDescriptor,
        private val valueParameters: List<ParameterDescriptor>,
    ) : FunctionSignature() {

        override val overloadResolutionSelector: String
            get() = "$file#${extensionReceiver.type.upperBoundFqName}@${name.identifier}(${valueParameters.joinNamesWithTypes()})"

        override fun copy(valueParameters: List<ParameterDescriptor>): FunctionSignature =
            InterfaceExtension(file, name, extensionReceiver, valueParameters)
    }

    protected fun List<ParameterDescriptor>.joinNamesWithTypes(): String =
        this.joinToString(", ") { "${it.name}: ${it.type.upperBoundFqName}" }

    protected val KotlinType.upperBoundFqName: String
        get() = if (this.constructor.declarationDescriptor is TypeParameterDescriptor) {
            this.constructor.supertypes.firstOrNull()?.upperBoundFqName
        } else {
            null
        } ?: this.constructor.declarationDescriptor?.fqNameOrNull()?.toString() ?: this.toString()
}

internal fun FunctionDescriptor.toFunctionSignature(): FunctionSignature = when {
    this is ConstructorDescriptor -> FunctionSignature.Constructor(this.constructedClass, this.valueParameters)
    this.dispatchReceiverParameter == null && this.extensionReceiverParameter == null -> {
        FunctionSignature.GlobalFunction(this.findSourceFile().name, this.name, this.valueParameters)
    }
    this.dispatchReceiverParameter == null &&
        this.extensionReceiverParameter?.type?.isInterface() == true -> {
        FunctionSignature.InterfaceExtension(this.findSourceFile().name, this.name, this.extensionReceiverParameter!!, this.valueParameters)
    }
    this.dispatchReceiverParameter == null -> {
        FunctionSignature.ClassExtension(this.name, this.extensionReceiverParameter!!, this.valueParameters)
    }
    else -> FunctionSignature.ClassMethod(
        this.name,
        this.dispatchReceiverParameter!!,
        this.extensionReceiverParameter,
        this.valueParameters
    )
}
