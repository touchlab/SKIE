package `tests`.`sealed`.`interfaces`.`collision`.`nested_and_outer`.`child`.`is_solved_by_renaming`

import co.touchlab.swiftgen.api.SealedInterop

sealed interface A {

    @SealedInterop.Case.Name("X")
    class A1 : A
}

class A1 : A