package tests.sealed.classes.configuration.parent.enabled

import co.touchlab.swiftgen.api.SwiftSealed

@SwiftSealed.Enabled
sealed class A

class A1 : A()
class A2 : A()