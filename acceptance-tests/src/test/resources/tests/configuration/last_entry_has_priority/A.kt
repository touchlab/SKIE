package `tests`.`configuration`.`last_entry_has_priority`

import co.touchlab.swiftgen.api.SealedInterop

sealed class A

@SealedInterop.Case.Name("A1")
class A1 : A()