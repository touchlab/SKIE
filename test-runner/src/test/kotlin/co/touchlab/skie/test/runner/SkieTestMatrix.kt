package co.touchlab.skie.test.runner

data class SkieTestMatrix(
    val axes: List<Axis<*>>,
) {
    val values = axes.associateBy { it.type }

    fun <T> mapCells(transformCell: (List<AxisValue>) -> T): List<T> {
        val cartesianProduct = axes.fold(listOf(emptyList<AxisValue>())) { acc, axis ->
            acc.flatMap { list ->
                axis.values.map { element ->
                    list + AxisValue(axis.type, axis.name, element)
                }
            }
        }

        return cartesianProduct.map(transformCell)
    }

    data class Axis<T: Any>(
        val type: Class<T>,
        val name: String,
        val values: List<T>,
    ) {
        companion object {
            inline operator fun <reified T: Any> invoke(name: String, values: List<T>): Axis<T> {
                return Axis(
                    type = T::class.java,
                    name = name,
                    values = values,
                )
            }
        }
    }

    data class AxisValue(
        val type: Class<*>,
        val name: String,
        val value: Any,
    )
}
