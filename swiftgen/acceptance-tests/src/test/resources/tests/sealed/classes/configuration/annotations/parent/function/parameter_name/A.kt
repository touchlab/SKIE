package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`parent`.`function`.`parameter_name`

import co.touchlab.swiftgen.api.SealedInterop

@SealedInterop.Function.ParameterName("a")
@SealedInterop.Function.ArgumentLabel("")
sealed class A

class A1 : A()
class A2 : A()
