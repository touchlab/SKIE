package tests.bugs.flow_with_string

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

val flow: Flow<String> = flowOf("A")
