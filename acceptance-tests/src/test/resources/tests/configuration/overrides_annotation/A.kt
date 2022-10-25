package `tests`.`configuration`.`overrides_annotation`

import co.touchlab.skie.configuration.SealedInterop

sealed class A

@SealedInterop.Case.Name("A1")
class A1 : A()
