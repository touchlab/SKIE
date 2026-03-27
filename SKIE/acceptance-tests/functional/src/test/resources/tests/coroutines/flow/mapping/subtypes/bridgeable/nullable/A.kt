package `tests`.`coroutines`.`flow`.`mapping`.`subtypes`.`bridgeable`.`nullable`

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val flow: StateFlow<String?> = MutableStateFlow("A")
