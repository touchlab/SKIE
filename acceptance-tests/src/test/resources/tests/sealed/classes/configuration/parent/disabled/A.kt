package tests.sealed.classes.configuration.parent.disabled

import co.touchlab.swiftgen.api.SwiftSealed

@SwiftSealed.Disabled
sealed class A

class A1 : A()
class A2 : A()