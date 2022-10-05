package tests.enums.configuration.annotations.enabled

import co.touchlab.swiftgen.api.EnumInterop

@EnumInterop.Enabled
enum class A {
    A1,
    A2,
}

fun a1(): A {
    return A.A1
}
