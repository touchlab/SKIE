package tests.sealed.interfaces.configuration.child.visible

import co.touchlab.swiftgen.api.SwiftSealedCase

sealed interface A

@SwiftSealedCase.Visible
class A1(val i: Int) : A
class A2(val k: Int) : A