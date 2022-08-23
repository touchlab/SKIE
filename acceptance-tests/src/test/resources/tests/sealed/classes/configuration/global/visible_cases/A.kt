package tests.sealed.classes.configuration.global.child.visible_cases

import co.touchlab.swiftgen.api.SwiftSealedCase

sealed class A

class A1(val i: Int) : A()

@SwiftSealedCase.Visible
class A2(val k: Int) : A()
