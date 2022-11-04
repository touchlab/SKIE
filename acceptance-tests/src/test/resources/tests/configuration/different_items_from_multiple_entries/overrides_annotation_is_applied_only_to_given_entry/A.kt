package `tests`.`configuration`.`different_items_from_multiple_entries`.`overrides_annotation_is_applied_only_to_given_entry`

import co.touchlab.skie.configuration.annotations.SealedInterop

@SealedInterop.ElseName("Else")
sealed class A

@SealedInterop.Case.Name("A1")
class A1 : A()

@SealedInterop.Case.Hidden
class A2 : A()
