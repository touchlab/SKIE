package tests.bugs.flows_cannot_be_globally_disabled

import kotlinx.coroutines.flow.MutableStateFlow

fun foo(): MutableStateFlow<Int> = TODO()
