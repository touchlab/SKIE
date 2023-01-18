package co.touchlab.skie.test.exported

interface ExportedInterface {
    enum class NestedEnumInInterface {
        A, B, C
    }

    object NestedObjectInInterface

    interface NestedInterfaceInInterface

    class NestedClassInInterface

    companion object
}
