package tests.sealed.interfaces.configuration.annotations.child.name

import co.touchlab.swiftgen.api.SwiftSealedCase

sealed interface A

@SwiftSealedCase.Name("A3")
class A1(val i: Int) : A
class A2(val k: Int) : A