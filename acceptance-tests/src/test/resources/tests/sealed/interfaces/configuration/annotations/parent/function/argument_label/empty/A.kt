package `tests`.`sealed`.`interfaces`.`configuration`.`annotations`.`parent`.`function`.`argument_label`.`empty`

import co.touchlab.skie.configuration.annotations.SealedInterop

@SealedInterop.Function.ArgumentLabel("")
sealed interface A

class A1 : A
class A2 : A
