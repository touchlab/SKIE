package co.touchlab.skie.test

import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.joinToCode
import io.outfoxx.swiftpoet.parameterizedBy

object TestFileGenerationPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val kirClass = kirProvider.getClassByFqName("co.touchlab.skie.test.KotlinFile")
        val sirClass = kirClass.originalSirClass

        namespaceProvider.getSkieNamespaceWrittenSourceFile("TypeVerification").content = """
                @resultBuilder
                struct VerifyReturnType<ReturnType> {
                    let value: ReturnType

                    static func buildBlock<T>(_ components: T...) -> VerifyReturnType<T> {
                        return VerifyReturnType<T>(value: components[0])
                    }

                    static func buildFinalResult(_ component: VerifyReturnType<ReturnType>) -> ReturnType {
                        return component.value
                    }
                }

                @propertyWrapper
                struct VerifyParamType<Final> {
                    let wrappedValue: Final

                    var projectedValue: VerifyParamType<Final> {
                        self
                    }

                    init(wrappedValue: Final) {
                        self.wrappedValue = wrappedValue
                    }

                    init(projectedValue: VerifyParamType<Final>) {
                        self.wrappedValue = projectedValue.wrappedValue
                    }
                }
            """.trimIndent()

        val swiftFileBuilders = mutableListOf<FileSpec.Builder>()

        FileSpec.builder(framework.frameworkName, "TypeVerification+verify")
            .also { swiftFileBuilders.add(it) }
            .apply {
                val parameterCounts = kirClass.callableDeclarations
                    // TODO Remove once CreateKirDescriptionAndHashPropertyPhase is enabled
                    .filterNot { it is KirSimpleFunction && (it.kotlinName == "toString" || it.kotlinName == "hashCode") }
                    .asSequence()
                    .map { it.primarySirDeclaration }
                    .filterIsInstance<SirFunction>()
                    .map { it.valueParameters.size }
                    .filter { it >= 1 }
                    .distinct()

                /**
                 * func verify<P1, T>(function: @escaping (P1) throws -> T) -> (VerifyParamType<P1>) -> T {
                 *     return { $p1 in
                 *         try! function(p1)
                 *     }
                 * }
                 */
                parameterCounts.forEach { paramCount ->
                    val paramTypeVariables = (1..paramCount).map {
                        TypeVariableName.typeVariable("P$it")
                    }
                    val returnTypeVariable = TypeVariableName.typeVariable("T")

                    addFunction(
                        FunctionSpec.builder("verify")
                            .addTypeVariables(paramTypeVariables)
                            .addTypeVariable(returnTypeVariable)
                            .addParameter(
                                "function",
                                FunctionTypeName.get(
                                    parameters = paramTypeVariables.map {
                                        ParameterSpec.unnamed(it)
                                    },
                                    returnType = returnTypeVariable,
                                    attributes = listOf(AttributeSpec.ESCAPING),
                                    throws = true,
                                ),
                            )
                            .returns(
                                FunctionTypeName.get(
                                    parameters = paramTypeVariables.map {
                                        ParameterSpec.unnamed(DeclaredTypeName.qualifiedTypeName(".VerifyParamType").parameterizedBy(it))
                                    },
                                    returnType = returnTypeVariable,
                                ),
                            )
                            .addCode(
                                """
                                return { %L in
                                    try! function(%L)
                                }
                            """.trimIndent(),
                                paramTypeVariables.indices.map { CodeBlock.of("${"$"}p$it") }.joinToCode(),
                                paramTypeVariables.indices.map { CodeBlock.of("p$it") }.joinToCode(),
                            )
                            .build(),
                    )
                }
            }

        FileSpec.builder(framework.frameworkName, "KotlinFile_access")
            .also { swiftFileBuilders.add(it) }
            .apply {
                addImport("Foundation")

                addType(
                    TypeSpec.classBuilder("KotlinFileWrapper")
                        .apply {
                            val parametrizedKotlinClass = sirClass.toTypeFromEnclosingTypeParameters(sirClass.typeParameters).evaluate().swiftPoetTypeName

                            addProperty(
                                PropertySpec.builder("kotlinClass", parametrizedKotlinClass)
                                    .initializer("%T()", parametrizedKotlinClass)
                                    .build(),
                            )

                            sirClass.typeParameters
                                .map { typeParameter ->
                                    val bounds = typeParameter.bounds.map { bound ->
                                        val constraint = when (bound) {
                                            is SirTypeParameter.Bound.Conformance -> TypeVariableName.Bound.Constraint.CONFORMS_TO
                                            is SirTypeParameter.Bound.Equality -> TypeVariableName.Bound.Constraint.SAME_TYPE
                                        }
                                        TypeVariableName.bound(constraint, bound.type.evaluate().swiftPoetTypeName)
                                    }

                                    TypeVariableName.typeVariable(typeParameter.name, bounds)
                                }
                                .forEach { typeVariable ->
                                    addTypeVariable(typeVariable)
                                }

                            kirClass.callableDeclarations
                                // TODO Remove once CreateKirDescriptionAndHashPropertyPhase is enabled
                                .filterNot { it is KirSimpleFunction && (it.kotlinName == "toString" || it.kotlinName == "hashCode") }
                                .map { it.primarySirDeclaration }
                                .forEach { callableDeclaration ->
                                    when (callableDeclaration) {
                                        is SirProperty -> {
                                            addProperty(
                                                PropertySpec.builder(
                                                    callableDeclaration.identifier,
                                                    callableDeclaration.type.evaluate().swiftPoetTypeName,
                                                )
                                                    .addAttribute(
                                                        CodeBlock.of(
                                                            "VerifyReturnType<%T>",
                                                            callableDeclaration.type.evaluate().swiftPoetTypeName,
                                                        )
                                                            .toString(),
                                                    )
                                                    .getter(
                                                        FunctionSpec.getterBuilder()
                                                            .addStatement("kotlinClass.%L", callableDeclaration.reference)
                                                            .build(),
                                                    )
                                                    .build(),
                                            )
                                        }
                                        is SirSimpleFunction -> {
                                            addFunction(
                                                FunctionSpec.builder(callableDeclaration.identifier)
                                                    .addAttribute(
                                                        CodeBlock.of(
                                                            "VerifyReturnType<%T>",
                                                            callableDeclaration.returnType.evaluate().swiftPoetTypeName,
                                                        ).toString(),
                                                    )
                                                    .addParameters(
                                                        callableDeclaration.valueParameters.map { parameter ->
                                                            ParameterSpec
                                                                .builder(
                                                                    parameter.label,
                                                                    parameter.name,
                                                                    parameter.type.evaluate().swiftPoetTypeName,
                                                                )
                                                                .addAttribute("VerifyParamType")
                                                                .build()
                                                        },
                                                    )
                                                    .returns(callableDeclaration.returnType.evaluate().swiftPoetTypeName)
                                                    .addCode(
                                                        """
                                                        let functionVerification = verify(function: kotlinClass.%N)
                                                        functionVerification(%L)
                                                    """.trimIndent(),
                                                        callableDeclaration.reference,
                                                        callableDeclaration.valueParameters.map { parameter ->
                                                            CodeBlock.of("_%N", parameter.name)
                                                        }.joinToCode(),
                                                    )
                                                    .build(),
                                            )
                                        }
                                        is SirConstructor -> {
                                            //
                                        }
                                    }
                                }
                        }
                        .build(),
                )
            }

        swiftFileBuilders.forEach { builder ->
            val file = builder.build()

            namespaceProvider.getSkieNamespaceWrittenSourceFile(file.name).content = file.toString()
        }
    }
}
