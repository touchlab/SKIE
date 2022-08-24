package `tests`.`sealed`.`interfaces`.`no_accessible_children`.`configuration`

import co.touchlab.swiftgen.api.SealedInterop

sealed interface A

@SealedInterop.Case.Hidden
class A1 : A

@SealedInterop.Case.Hidden
class A2 : A