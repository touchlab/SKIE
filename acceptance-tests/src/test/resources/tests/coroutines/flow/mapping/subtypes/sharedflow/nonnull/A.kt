package tests.coroutines.flow.mapping.subtypes.sharedflow.nonnull

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn

val flow: SharedFlow<Int> = flowOf(1, 2, 3).shareIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, 2)
