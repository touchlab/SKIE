@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.test

import co.touchlab.skie.plugin.api.descriptorProvider
import co.touchlab.skie.plugin.api.kotlin.getAllExposedMembers
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.stableSpec
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.intercept.PhaseListener
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.joinToCode
import io.outfoxx.swiftpoet.parameterizedBy
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.resolve.DescriptorUtils

internal class SwiftKotlinAccessGenerator: PhaseListener {
    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJC_EXPORT

    override fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.afterPhase(phaseConfig, phaserState, context)

        val descriptorProvider = context.descriptorProvider

        val kotlinClass = descriptorProvider.transitivelyExposedClasses.first {
            it.name.identifier == "KotlinFile"
        }

        context.skieContext.module.file("KotlinFile_access") {
            addType(
                TypeSpec.classBuilder("KotlinFileWrapper")
                    .apply {
                        val typeVariables = kotlinClass.declaredTypeParameters.map { typeParameter ->
                            TypeVariableName(typeParameter.name.identifier, TypeVariableName.bound(ANY_OBJECT))
                        }
                        val parametrizedKotlinClass = if (typeVariables.isNotEmpty()) {
                            kotlinClass.swiftModel.stableSpec.parameterizedBy(*typeVariables.toTypedArray())
                        } else {
                            kotlinClass.swiftModel.stableSpec
                        }

                        addProperty(
                            PropertySpec.builder("kotlinClass", parametrizedKotlinClass)
                                .initializer("%T()", parametrizedKotlinClass)
                                .build()
                        )

                        typeVariables.forEach { typeVariable ->
                            addTypeVariable(typeVariable)
                        }

                        descriptorProvider.getAllExposedMembers(kotlinClass)
                            .filter {
                                !DescriptorUtils.isMethodOfAny(it)
                            }
                            .flatMap {
                                it.swiftModel.directlyCallableMembers
                            }
                            .forEach { swiftModel ->
                                when (swiftModel) {
                                    is KotlinRegularPropertySwiftModel -> {
                                        addProperty(
                                            PropertySpec.builder(swiftModel.identifier, swiftModel.type.stableSpec)
                                                .getter(
                                                    FunctionSpec.getterBuilder()
                                                        .addStatement("kotlinClass.%L", swiftModel.reference)
                                                        .build()
                                                )
                                                .build()
                                        )
                                    }
                                    is KotlinFunctionSwiftModel -> {
                                        if (swiftModel.role == KotlinFunctionSwiftModel.Role.Constructor) {
                                            return@forEach
                                        }
                                        addFunction(
                                            FunctionSpec.builder(swiftModel.identifier)
                                                .addParameters(
                                                    swiftModel.valueParameters.map { parameter ->
                                                        ParameterSpec.builder(
                                                            parameter.argumentLabel,
                                                            parameter.parameterName,
                                                            parameter.type.stableSpec,
                                                        ).build()
                                                    }
                                                )
                                                .returns(swiftModel.returnType.stableSpec)
                                                .addCode(
                                                    "kotlinClass.%N(%L)",
                                                    swiftModel.reference,
                                                    swiftModel.valueParameters.map { parameter ->
                                                        CodeBlock.of("%N", parameter.parameterName)
                                                    }.joinToCode(),
                                                )
                                                .build()
                                        )
                                    }
                                }
                            }
                    }
                    .build()
            )
        }

    }
}
