package `tests`.`sealed`.`classes`.`configuration`.`global`.`parent`.`disabled`

import co.touchlab.swiftgen.api.SealedInterop

@SealedInterop.Enabled
sealed class A

class A1 : A()
class A2 : A()