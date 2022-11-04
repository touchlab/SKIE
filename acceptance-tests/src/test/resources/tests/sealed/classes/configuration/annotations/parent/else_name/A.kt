package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`parent`.`else_name`

import co.touchlab.skie.configuration.annotations.SealedInterop

@SealedInterop.ElseName("Other")
sealed class A

@SealedInterop.Case.Hidden
class A1(val i: Int) : A()
class A2(val k: Int) : A()
