package `tests`.`sealed`.`interfaces`.`configuration`.`annotations`.`parent`.`function`.`argument_label`.`removed`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Function.ArgumentLabel("_")
sealed interface A

class A1 : A
class A2 : A
