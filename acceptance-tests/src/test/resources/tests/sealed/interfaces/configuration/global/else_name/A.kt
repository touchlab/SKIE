package tests.sealed.interfaces.configuration.global.else_name

import co.touchlab.swiftgen.api.SwiftSealedCase

sealed interface A

@SwiftSealedCase.Hidden
class A1(val i: Int) : A
class A2(val k: Int) : A