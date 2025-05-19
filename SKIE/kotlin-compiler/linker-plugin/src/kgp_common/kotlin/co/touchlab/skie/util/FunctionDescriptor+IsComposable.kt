package co.touchlab.skie.util

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.FqName

val FunctionDescriptor.isComposable: Boolean
    get() = this.annotations.hasAnnotation(FqName("androidx.compose.runtime.Composable")) || this.valueParameters.any {
        it.type.annotations.hasAnnotation(FqName("androidx.compose.runtime.Composable"))
    }
