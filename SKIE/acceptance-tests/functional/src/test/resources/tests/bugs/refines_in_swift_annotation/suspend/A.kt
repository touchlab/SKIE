package `tests`.`bugs`.`refines_in_swift_annotation`.`suspend`

import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
@ShouldRefineInSwift
suspend fun foo(): Int = 0
