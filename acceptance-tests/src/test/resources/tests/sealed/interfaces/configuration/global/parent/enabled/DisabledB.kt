package `tests`.`sealed`.`interfaces`.`configuration`.`global`.`parent`.`enabled`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Disabled
sealed interface B

class B1 : B
class B2 : B
