package co.touchlab.skie.sir.element

sealed interface SirConditionalConstraintParent {
    val conditionalConstraints: MutableList<SirConditionalConstraint>

    object None : SirConditionalConstraintParent {
        override val conditionalConstraints: MutableList<SirConditionalConstraint>
            get() = mutableListOf()

        override fun toString(): String = "${SirConditionalConstraintParent::class.simpleName}.${this::class.simpleName}"
    }
}

