package tests.sealed.classes.configuration.child.hidden

import co.touchlab.swiftgen.api.SwiftSealedCase

sealed class A

@SwiftSealedCase.Hidden
class A1(val i: Int) : A()
class A2(val k: Int) : A()