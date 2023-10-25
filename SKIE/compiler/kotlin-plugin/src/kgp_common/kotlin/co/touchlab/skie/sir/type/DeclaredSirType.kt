package co.touchlab.skie.sir.type

sealed class DeclaredSirType : NonNullSirType() {

    abstract val pointsToInternalName: Boolean

    abstract fun withFqName(): DeclaredSirType

    abstract override fun evaluate(): EvaluatedSirType<SirDeclaredSirType>
}
