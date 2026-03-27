package co.touchlab.skie.test.exported

interface ExportedSingleParamInterface<T> {
    enum class NestedEnumInGenericInterface {
        A, B, C
    }

    object NestedObjectInGenericInterface

    interface NestedInterfaceInGenericInterface

    class NestedClassInGenericInterface

    companion object
}
