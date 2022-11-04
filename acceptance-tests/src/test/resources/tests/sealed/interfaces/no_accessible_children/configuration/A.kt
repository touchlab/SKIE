package `tests`.`sealed`.`interfaces`.`no_accessible_children`.`configuration`

import co.touchlab.skie.configuration.annotations.SealedInterop

sealed interface A

@SealedInterop.Case.Hidden
class A1 : A

@SealedInterop.Case.Hidden
class A2 : A
