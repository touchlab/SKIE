package tests.sealed.interfaces.configuration.global.child.visible_cases

import co.touchlab.swiftgen.api.SwiftSealedCase

sealed interface A

class A1(val i: Int) : A

@SwiftSealedCase.Visible
class A2(val k: Int) : A
