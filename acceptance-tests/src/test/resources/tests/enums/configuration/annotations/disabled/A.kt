package tests.enums.configuration.annotations.disabled

import co.touchlab.swiftgen.api.EnumInterop

@EnumInterop.Disabled
enum class A {
    A1,
    A2,
}

fun a1(): A {
    return A.A1
}
