package `tests`.`sealed`.`interfaces`.`configuration`.`annotations`.`parent`.`enabled`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Enabled
sealed interface A

class A1 : A
class A2 : A
