package `tests`.`sealed`.`interfaces`.`configuration`.`annotations`.`parent`.`function`.`argument_label`.`changed`

import co.touchlab.swiftgen.api.SealedInterop

@SealedInterop.Function.ArgumentLabel("a")
sealed interface A

class A1 : A
class A2 : A
