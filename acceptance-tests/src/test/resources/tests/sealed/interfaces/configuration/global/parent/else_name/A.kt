package `tests`.`sealed`.`interfaces`.`configuration`.`global`.`parent`.`else_name`

import co.touchlab.swiftgen.api.SealedInterop

sealed interface A

@SealedInterop.Case.Hidden
class A1(val i: Int) : A
class A2(val k: Int) : A