package `tests`.`sealed`.`classes`.`configuration`.`global`.`child`.`name`

import co.touchlab.skie.configuration.SealedInterop

sealed class A

class A1(val i: Int) : A()

@SealedInterop.Case.Name("A2")
class A2(val k: Int) : A()
