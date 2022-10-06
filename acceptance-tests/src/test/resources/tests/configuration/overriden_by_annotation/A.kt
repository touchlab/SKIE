package `tests`.`configuration`.`overriden_by_annotation`

import co.touchlab.swiftgen.api.SealedInterop

sealed class A

@SealedInterop.Case.Name("A1")
class A1 : A()
