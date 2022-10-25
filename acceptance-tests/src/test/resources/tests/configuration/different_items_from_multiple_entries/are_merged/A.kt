package `tests`.`configuration`.`different_items_from_multiple_entries`.`are_merged`

import co.touchlab.skie.configuration.SealedInterop

sealed class A

class A1 : A()

@SealedInterop.Case.Hidden
class A2 : A()
