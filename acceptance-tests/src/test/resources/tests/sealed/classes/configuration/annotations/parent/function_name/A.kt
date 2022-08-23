package tests.sealed.classes.configuration.annotations.parent.function_name

import co.touchlab.swiftgen.api.SwiftSealed

@SwiftSealed.FunctionName("exhaustively2")
sealed class A

class A1 : A()
class A2 : A()