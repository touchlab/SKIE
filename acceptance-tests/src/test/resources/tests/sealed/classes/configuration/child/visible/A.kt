package tests.sealed.classes.configuration.child.visible

import co.touchlab.swiftgen.api.SwiftSealedCase

sealed class A

@SwiftSealedCase.Visible
class A1(val i: Int) : A()
class A2(val k: Int) : A()