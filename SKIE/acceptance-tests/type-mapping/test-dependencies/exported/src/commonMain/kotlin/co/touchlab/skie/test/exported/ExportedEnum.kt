package co.touchlab.skie.test.exported

enum class ExportedEnum {
    A, B, C;

    enum class NestedEnumInEnum {
        A, B, C
    }

    object NestedObjectInEnum

    interface NestedInterfaceInEnum

    class NestedClassInEnum

    companion object
}
