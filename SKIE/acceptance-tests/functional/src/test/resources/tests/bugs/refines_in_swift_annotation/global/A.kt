package `tests`.`bugs`.`refines_in_swift_annotation`.`global`

import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
@ShouldRefineInSwift
fun foo(): Int = 0

@OptIn(ExperimentalObjCRefinement::class)
@ShouldRefineInSwift
val bar: Int = 0

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun removed(): Int = 0
