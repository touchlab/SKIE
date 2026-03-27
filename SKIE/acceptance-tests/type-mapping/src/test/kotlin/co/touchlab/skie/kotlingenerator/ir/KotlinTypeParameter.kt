package co.touchlab.skie.kotlingenerator.ir

class KotlinTypeParameter private constructor(
    val name: String,
    val bounds: List<KotlinType>,
) {

    init {
        val sortedBounds = bounds.sortedBy { it.getSafeName(listOf(this)) }

        check(bounds == sortedBounds) {
            "Bounds must be sorted by their name. Was: ${bounds.map { it.getSafeName(listOf(this)) }}"
        }
    }

    fun toUsage(): KotlinType.TypeParameterUsage =
        KotlinType.TypeParameterUsage(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KotlinTypeParameter

        return name == other.name
    }

    override fun hashCode(): Int =
        name.hashCode()

    companion object {

        operator fun invoke(name: String?, vararg bounds: KotlinType): KotlinTypeParameter {
            val sortedBounds = bounds.sortedBy { it.getSafeName() }

            val name = name ?: getComputedName(sortedBounds, null)

            return KotlinTypeParameter(name, sortedBounds)
        }

        operator fun invoke(name: String? = null, boundsFactory: (KotlinType.TypeParameterUsage) -> List<KotlinType>): KotlinTypeParameter {
            val name = if (name == null) {
                val dummyKotlinTypeParameter = KotlinTypeParameter("<NO-NAME>", emptyList())

                val dummyBounds = boundsFactory(KotlinType.TypeParameterUsage(dummyKotlinTypeParameter)).sortedBy {
                    it.getSafeName(listOf(dummyKotlinTypeParameter))
                }

                getComputedName(dummyBounds, dummyKotlinTypeParameter)
            } else {
                name
            }

            val bounds = mutableListOf<KotlinType>()

            val actualKotlinTypeParameter = KotlinTypeParameter(name, bounds)

            val actualBounds = boundsFactory(KotlinType.TypeParameterUsage(actualKotlinTypeParameter)).sortedBy {
                it.getSafeName(listOf(actualKotlinTypeParameter))
            }

            bounds.addAll(actualBounds)

            return actualKotlinTypeParameter
        }

        private fun getComputedName(bounds: List<KotlinType>, typeParameter: KotlinTypeParameter?): String =
            if (bounds.isNotEmpty()) {
                bounds.joinToString("_") {
                    it.getSafeName(listOfNotNull(typeParameter))
                }.uppercase()
            } else {
                "NO_BOUNDS"
            }
    }
}
