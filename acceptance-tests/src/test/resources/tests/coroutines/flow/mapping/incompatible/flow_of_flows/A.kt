package tests.coroutines.flow.mapping.incompatible.flow_of_flows

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

fun flow(): Flow<Flow<Int>> = flowOf(flowOf(1))
