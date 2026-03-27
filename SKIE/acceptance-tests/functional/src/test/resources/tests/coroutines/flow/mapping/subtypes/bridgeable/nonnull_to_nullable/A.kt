package `tests`.`coroutines`.`flow`.`mapping`.`subtypes`.`bridgeable`.`nonnull_to_nullable`

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val flow: StateFlow<String> = MutableStateFlow("A")
