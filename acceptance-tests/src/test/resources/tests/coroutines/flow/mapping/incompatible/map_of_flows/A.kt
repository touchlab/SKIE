package tests.coroutines.flow.mapping.incompatible.map_of_flows

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

fun mapValue(): Map<Int, Flow<Int>> = mapOf(1 to flowOf(1))
