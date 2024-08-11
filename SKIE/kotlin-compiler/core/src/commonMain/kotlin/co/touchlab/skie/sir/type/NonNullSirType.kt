package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.util.map
import io.outfoxx.swiftpoet.TupleTypeName

sealed class NonNullSirType : SirType()

data class TupleSirType(
    val elements: List<Element>,
) : NonNullSirType() {
    override val isHashable: Boolean
        get() = elements.all { it.type.isHashable }

    override val isReference: Boolean = false

    override fun evaluate(): EvaluatedSirType {
        val lazyEvaluatedTypes = lazy { elements.map { it.name to it.type.evaluate() } }

        return EvaluatedSirType.Lazy(
            typeProvider = lazyEvaluatedTypes.map { evaluatedTypes ->
                copy(elements = evaluatedTypes.map { (name, evaluatedType) ->
                    Element(name, evaluatedType.type)
                })
            },
            canonicalNameProvider = lazyEvaluatedTypes.map { evaluatedTypes ->
                evaluatedTypes.joinToString(", ", prefix = "(", postfix = ")") {  (name, evaluatedType) ->
                    if (name.isNotBlank()) {
                        "${name}: ${evaluatedType.canonicalName}"
                    } else {
                        evaluatedType.canonicalName
                    }
                }
            },
            swiftPoetTypeNameProvider = lazyEvaluatedTypes.map { evaluatedTypes ->
                TupleTypeName.of(evaluatedTypes.map { (name, evaluatedType) ->
                    name to evaluatedType.swiftPoetTypeName
                })
            },
            lowestVisibility = lazyEvaluatedTypes.map { evaluatedTypes ->
                evaluatedTypes.minOf { (_, evaluatedType) ->
                    evaluatedType.visibilityConstraint
                }
            },
            referencedTypeDeclarationsProvider = lazyEvaluatedTypes.map { evaluatedTypes ->
                evaluatedTypes.flatMap { (_, evaluatedType) ->
                    evaluatedType.referencedTypeDeclarations
                }.toSet()
            }
        )
    }

    override fun inlineTypeAliases(): TupleSirType =
        copy(elements = elements.map {
            it.copy(type = it.type.inlineTypeAliases())
        })

    override fun asHashableType(): TupleSirType? {
        return copy(elements = elements.map { element ->
            val hashableType = element.type.asHashableType() ?: return null
            element.copy(type = hashableType)
        })
    }

    override fun asReferenceType(): SirType? = null

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): TupleSirType =
        copy(elements = elements.map {
            it.copy(type = it.type.substituteTypeParameters(substitutions))
        })

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): TupleSirType =
        copy(elements = elements.map {
            it.copy(type = it.type.substituteTypeArguments(substitutions))
        })

    data class Element(
        val name: String,
        val type: SirType,
    )

    companion object {
        operator fun invoke(
            vararg elements: Pair<String, SirType>,
        ) = TupleSirType(
            elements = elements.map { Element(it.first, it.second) },
        )

        operator fun invoke(
            vararg elements: SirType,
        ) = TupleSirType(
            elements = elements.toList(),
        )

        operator fun invoke(
            elements: List<SirType>,
        ) = TupleSirType(
            elements = elements.map { Element("", it) }
        )
    }
}
