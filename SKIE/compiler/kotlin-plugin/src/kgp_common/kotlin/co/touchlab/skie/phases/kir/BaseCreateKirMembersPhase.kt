@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirScope
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.phases.CompilerDependentDescriptorConversionPhase
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass

internal abstract class BaseCreateKirMembersPhase(
    context: CompilerDependentDescriptorConversionPhase.Context,
) : CompilerDependentDescriptorConversionPhase {

    protected val descriptorProvider = context.descriptorProvider
    protected val mapper = context.mapper
    protected val kirProvider = context.kirProvider
    protected val descriptorKirProvider = context.descriptorKirProvider
    protected val descriptorConfigurationProvider = context.descriptorConfigurationProvider
    protected val kirDeclarationTypeTranslator = context.kirDeclarationTypeTranslator
    protected val namer = context.namer

    protected val PropertyDescriptor.baseProperty: PropertyDescriptor
        get() = (getAllParents(this) + this.original).first { mapper.isBaseProperty(it) }

    private fun getAllParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    protected fun getDirectParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }
            .filter { mapper.shouldBeExposed(it) }

    protected val FunctionDescriptor.baseFunction: FunctionDescriptor
        get() = (getAllParents(this) + this.original).first { mapper.isBaseMethod(it) }

    private fun getAllParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    protected fun getDirectParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }
            .filter { mapper.shouldBeExposed(it) }

    protected val CallableMemberDescriptor.kirDeprecationLevel: DeprecationLevel
        get() {
            val deprecationInfo = mapper.getDeprecation(this)

            return when (deprecationInfo?.deprecationLevel) {
                DeprecationLevelValue.ERROR -> DeprecationLevel.Error(deprecationInfo.message)
                DeprecationLevelValue.WARNING -> DeprecationLevel.Warning(deprecationInfo.message)
                DeprecationLevelValue.HIDDEN -> DeprecationLevel.Error(deprecationInfo.message)
                null -> DeprecationLevel.None
            }
        }

    protected val CallableMemberDescriptor.signature: String
        get() = with(KonanManglerDesc) {
            this@signature.signatureString(false)
        }

    protected val KirClass.callableDeclarationScope: KirScope
        get() = when (this.kind) {
            KirClass.Kind.File -> KirScope.Static
            else -> KirScope.Member
        }

    protected val CallableMemberDescriptor.isRefinedInSwift: Boolean
        get() = annotations.any { annotation ->
            annotation.annotationClass?.annotations?.any { it.fqName == KonanFqNames.refinesInSwift } == true
        }

    protected val MethodBridge.ReturnValue.errorHandlingStrategy: OirFunction.ErrorHandlingStrategy
        get() = when (this) {
            MethodBridge.ReturnValue.WithError.Success -> OirFunction.ErrorHandlingStrategy.ReturnsBoolean
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                if (this.successMayBeZero) {
                    OirFunction.ErrorHandlingStrategy.SetsErrorOut
                } else {
                    OirFunction.ErrorHandlingStrategy.ReturnsZero
                }
            }
            else -> OirFunction.ErrorHandlingStrategy.Crashes
        }
}
