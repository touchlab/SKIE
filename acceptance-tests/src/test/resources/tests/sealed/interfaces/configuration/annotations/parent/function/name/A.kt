package `tests`.`sealed`.`interfaces`.`configuration`.`annotations`.`parent`.`function`.`name`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Function.Name("onEnum2")
sealed interface A

class A1 : A
class A2 : A
