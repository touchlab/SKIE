package `tests`.`configuration`.`overrides_annotation`

import co.touchlab.skie.configuration.annotations.SealedInterop

sealed class A

@SealedInterop.Case.Name("A1")
class A1 : A()
