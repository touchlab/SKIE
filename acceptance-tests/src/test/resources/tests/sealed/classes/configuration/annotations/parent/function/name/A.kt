package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`parent`.`function`.`name`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Function.Name("onEnum2")
sealed class A

class A1 : A()
class A2 : A()
