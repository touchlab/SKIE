package `tests`.`sealed`.`classes`.`configuration`.`annotations`.`parent`.`enabled`

import co.touchlab.swiftgen.api.SealedInterop

@SealedInterop.Enabled
sealed class A

class A1 : A()
class A2 : A()