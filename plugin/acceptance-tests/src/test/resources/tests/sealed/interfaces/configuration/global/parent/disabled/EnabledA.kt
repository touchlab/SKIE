package `tests`.`sealed`.`interfaces`.`configuration`.`global`.`parent`.`disabled`

import co.touchlab.swiftgen.api.SealedInterop

@SealedInterop.Enabled
sealed interface A

class A1 : A
class A2 : A
