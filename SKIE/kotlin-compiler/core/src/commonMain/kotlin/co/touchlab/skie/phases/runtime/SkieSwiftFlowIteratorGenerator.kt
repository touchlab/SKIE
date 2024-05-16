package co.touchlab.skie.phases.runtime

import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.*

object SkieSwiftFlowIteratorGenerator {

    context(SirPhase.Context)
    fun generate(): SirClass {
        return namespaceProvider.getSkieNamespaceFile("SkieSwiftFlowIterator").run {
            SirClass(
                baseName = "SkieSwiftFlowIterator",
                superTypes = listOf(
                    sirBuiltins._Concurrency.AsyncIteratorProtocol.defaultType,
                ),
            ).apply {
                val tParameter = SirTypeParameter("T")
                val elementAlias = SirTypeAlias("Element") {
                    tParameter.toTypeParameterUsage()
                }

                addIteratorProperty()
                addInternalConstructor()
                addNextFunction(elementAlias)
                addCancelTaskFunction()
                addDeinit()
            }
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addIteratorProperty() {
        val skieColdFlowIterator = kirProvider
            .getClassByFqName("co.touchlab.skie.runtime.coroutines.flow.SkieColdFlowIterator")
            .originalSirClass

        SirProperty(
            identifier = "iterator",
            type = skieColdFlowIterator.toType(sirBuiltins.Swift.AnyObject.defaultType),
            visibility = SirVisibility.Private,
        )
    }

    context(SirPhase.Context)
    private fun SirClass.addInternalConstructor() {
        SirConstructor(
            visibility = SirVisibility.Internal,
        ).apply {
            SirValueParameter(
                name = "flow",
                type = SupportedFlow.Flow.getCoroutinesKirClass().originalSirClass.defaultType,
            )

            bodyBuilder.add {
                addCode("iterator = .init(flow: flow)")
            }
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addNextFunction(elementAlias: SirTypeAlias) {
        SirSimpleFunction(
            identifier = "next",
            returnType = sirBuiltins.Swift.Optional.toType(elementAlias.type),
            isAsync = true,
        ).apply {
            bodyBuilder.add {
                addCode("""
                            do {
                                let hasNext = try await skie(iterator).hasNext()

                                if hasNext.boolValue {
                                    return .some(iterator.next() as! Element)
                                } else {
                                    return nil
                                }
                            } catch is _Concurrency.CancellationError {
                                await cancelTask()

                                return nil
                            } catch {
                                Swift.fatalError("Unexpected error: \(error)")
                            }
                        """.trimIndent())
            }
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addCancelTaskFunction() {
        SirSimpleFunction(
            identifier = "cancelTask",
            returnType = sirBuiltins.Swift.Void.defaultType,
            visibility = SirVisibility.Private,
            isAsync = true,
        ).apply {
            bodyBuilder.add {
                addCode("""
                    _Concurrency.withUnsafeCurrentTask { task in
                        task?.cancel()
                    }
                """.trimIndent())
            }
        }
    }

    private fun SirClass.addDeinit() {
        deinitBuilder.add {
            addCode("iterator.cancel()")
        }
    }
}
