package tests.sealed.interfaces.configuration.annotations.child.visible

import co.touchlab.swiftgen.api.SealedInterop

sealed interface A

@SealedInterop.Case.Visible
class A1(val i: Int) : A
class A2(val k: Int) : A