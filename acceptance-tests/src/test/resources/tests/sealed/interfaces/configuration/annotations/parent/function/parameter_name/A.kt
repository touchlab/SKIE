package `tests`.`sealed`.`interfaces`.`configuration`.`annotations`.`parent`.`function`.`parameter_name`

import co.touchlab.skie.configuration.annotations.SealedInterop

@SealedInterop.Function.ParameterName("a")
@SealedInterop.Function.ArgumentLabel("")
sealed interface A

class A1 : A
class A2 : A
