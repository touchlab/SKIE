package tests.coroutines.flow.mapping.incompatible.list_of_flows

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

fun list(): List<Flow<Int>> = listOf(flowOf(1))
