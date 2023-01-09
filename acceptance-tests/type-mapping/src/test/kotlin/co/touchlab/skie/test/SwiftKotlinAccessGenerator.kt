@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.test

import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.internal.skieDescriptorProvider
import co.touchlab.skie.plugin.intercept.PhaseListener
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.VOID
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

internal class SwiftKotlinAccessGenerator: PhaseListener {
    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJC_EXPORT

    override fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.afterPhase(phaseConfig, phaserState, context)

        val kotlinClass = context.skieDescriptorProvider.classDescriptors.first {
            it.name.identifier == "KotlinFile"
        }
        context.skieContext.module.file("KotlinFile_access") {
            addProperty(
                PropertySpec.builder("kotlinClass", kotlinClass.spec)
                    .initializer("%T()", kotlinClass.spec)
                    .build()
            )
            addFunction(
                FunctionSpec.builder("anything")
                    .addTypeVariable(
                        TypeVariableName("T")
                    )
                    .addParameter("type", TypeVariableName("T.Type"))
                    .returns(TypeVariableName("T"))
                    .addCode("fatalError(\"Never called\")")
                    .build()
            )

            kotlinClass.unsubstitutedMemberScope.getContributedDescriptors()
                .filter {
                    (it as? CallableMemberDescriptor)?.overriddenDescriptors?.isEmpty() ?: true
                }
                .forEach { descriptor ->
                    when (descriptor) {
                        is PropertyDescriptor -> {
                            addProperty(
                                PropertySpec.builder(descriptor.name.identifier, descriptor.type.spec(KotlinTypeSpecUsage.Default))
                                    .initializer("kotlinClass.%L", descriptor.name.identifier)
                                    .build()
                            )
                        }
                        is FunctionDescriptor -> {
                            addFunction(
                                FunctionSpec.builder(descriptor.name.identifier)
                                    .addParameters(
                                        descriptor.valueParameters.map { parameter ->
                                            ParameterSpec.builder(
                                                parameter.name.identifier,
                                                parameter.type.spec(KotlinTypeSpecUsage.ParameterType)
                                            ).build()
                                        }
                                    )
                                    .async(descriptor.isSuspend)
                                    .throws(descriptor.isSuspend)
                                    .apply {
                                        when {
                                            descriptor.isSuspend -> {
                                                returns(descriptor.returnType?.spec(KotlinTypeSpecUsage.ReturnType.SuspendFunction) ?: VOID)
                                                addCode("try await kotlinClass.%L(value: value)", descriptor.name.identifier)
                                            }
                                            descriptor.extensionReceiverParameter != null -> {
                                                returns(descriptor.returnType?.spec(KotlinTypeSpecUsage.ReturnType) ?: VOID)
                                                addCode("kotlinClass.%L(value, value: value)", descriptor.name.identifier)
                                            }
                                            else -> {
                                                returns(descriptor.returnType?.spec(KotlinTypeSpecUsage.ReturnType) ?: VOID)
                                                addCode("kotlinClass.%L(value: value)", descriptor.name.identifier)
                                            }
                                        }
                                    }
                                    .build()
                            )
                        }
                    }
                }
        }

    }
}
