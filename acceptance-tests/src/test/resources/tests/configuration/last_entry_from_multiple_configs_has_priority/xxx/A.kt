package `tests`.`configuration`.`last_entry_from_multiple_configs_has_priority`.`xxx`

import co.touchlab.swiftgen.api.SealedInterop

sealed class A

@SealedInterop.Case.Name("A1")
class A1 : A()