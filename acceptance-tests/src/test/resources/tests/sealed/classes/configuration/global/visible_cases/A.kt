package `tests`.`sealed`.`classes`.`configuration`.`global`.`visible_cases`

import co.touchlab.swiftgen.api.SealedInterop

sealed class A

class A1(val i: Int) : A()

@SealedInterop.Case.Visible
class A2(val k: Int) : A()