package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.type.AirType
import co.touchlab.skie.plugin.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirClass(
    val symbol: Symbol,
    val name: Name,
    override val annotations: List<AirConstantObject>,
    override val origin: AirOrigin,
    val isExported: Boolean,
    val visibility: AirVisibility,
    /**
     * Contrary to IR does not contain enums and companion object.
     */
    val declarations: List<AirDeclaration>,
    val enumEntries: List<AirEnumEntry>,
    val companionObject: AirClass?,
    val typeParameters: List<AirTypeParameter>,
    val kind: Kind,
    val modality: AirModality,
    val isCompanion: Boolean,
    val isInner: Boolean,
    val isData: Boolean,
    val isExternal: Boolean,
    val isValue: Boolean,
    val isExpect: Boolean,
    val isFun: Boolean,
    val superTypes: List<AirType>,
    val sealedSubclasses: List<Symbol>,
) : AirDeclaration {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirClass =
        transformer.visitClass(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitClass(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
        typeParameters.forEach { it.accept(visitor, data) }
        declarations.forEach { it.accept(visitor, data) }
        enumEntries.forEach { it.accept(visitor, data) }
    }

    enum class Kind {
        Class, Interface, Enum, EnumEntry, Annotation, Object
    }

    @Serializable
    data class Name(val name: String)

    @Serializable
    data class Symbol(val id: Int) : AirTypeDeclaration.Symbol
}

val AirClass.Kind.isClass: Boolean
    get() = this == AirClass.Kind.Class

val AirClass.Kind.isInterface: Boolean
    get() = this == AirClass.Kind.Interface

val AirClass.Kind.isEnum: Boolean
    get() = this == AirClass.Kind.Enum

val AirClass.Kind.isEnumEntry: Boolean
    get() = this == AirClass.Kind.EnumEntry

val AirClass.Kind.isAnnotation: Boolean
    get() = this == AirClass.Kind.Annotation

val AirClass.Kind.isObject: Boolean
    get() = this == AirClass.Kind.Object
