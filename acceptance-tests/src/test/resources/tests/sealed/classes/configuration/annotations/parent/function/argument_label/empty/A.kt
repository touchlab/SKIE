package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`parent`.`function`.`argument_label`.`empty`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Function.ArgumentLabel("")
sealed class A

class A1 : A()
class A2 : A()
