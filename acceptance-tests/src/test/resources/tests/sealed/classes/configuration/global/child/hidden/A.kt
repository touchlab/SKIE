package `tests`.`sealed`.`classes`.`configuration`.`global`.`child`.`hidden`

import co.touchlab.skie.configuration.SealedInterop

sealed class A

class A1(val i: Int) : A()

@SealedInterop.Case.Visible
class A2(val k: Int) : A()
