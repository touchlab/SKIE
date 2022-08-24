package `tests`.`sealed`.`validation`.`children`.`hidden_visible_conflict`

import co.touchlab.swiftgen.api.SealedInterop

sealed class A {

    @SealedInterop.Case.Visible
    @SealedInterop.Case.Hidden
    class A1 : A()
}