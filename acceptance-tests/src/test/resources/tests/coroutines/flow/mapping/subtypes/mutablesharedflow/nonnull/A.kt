package tests.coroutines.flow.mapping.subtypes.mutablesharedflow.nonnull

import kotlinx.coroutines.flow.MutableSharedFlow

val flow: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1)
