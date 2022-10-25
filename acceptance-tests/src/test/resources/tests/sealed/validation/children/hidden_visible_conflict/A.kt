package `tests`.`sealed`.`validation`.`children`.`hidden_visible_conflict`

import co.touchlab.skie.configuration.SealedInterop

sealed class A {

    @SealedInterop.Case.Visible
    @SealedInterop.Case.Hidden
    class A1 : A()
}
