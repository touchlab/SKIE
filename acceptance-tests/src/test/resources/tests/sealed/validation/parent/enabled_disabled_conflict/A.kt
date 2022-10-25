package `tests`.`sealed`.`validation`.`parent`.`enabled_disabled_conflict`

import co.touchlab.skie.configuration.SealedInterop

@SealedInterop.Enabled
@SealedInterop.Disabled
sealed class A
