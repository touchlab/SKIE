package tests.bugs.flow_mapping_for_extension_receiver_of_flow_child

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

suspend fun StateFlow<Int>.foo(): Flow<Int> = this
