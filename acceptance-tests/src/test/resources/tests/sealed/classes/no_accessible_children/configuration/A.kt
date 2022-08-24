package tests.sealed.classes.no_accessible_children.configuration

import co.touchlab.swiftgen.api.SealedInterop

sealed class A

@SealedInterop.Case.Hidden
class A1 : A()

@SealedInterop.Case.Hidden
class A2 : A()

