package tests.sealed.interfaces.configuration.parent.else_name

import co.touchlab.swiftgen.api.SwiftSealed
import co.touchlab.swiftgen.api.SwiftSealedCase

@SwiftSealed.ElseName("Other")
sealed interface A

@SwiftSealedCase.Hidden
class A1(val i: Int) : A
class A2(val k: Int) : A