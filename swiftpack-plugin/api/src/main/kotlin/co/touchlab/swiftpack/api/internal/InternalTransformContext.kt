package co.touchlab.swiftpack.api.internal

import co.touchlab.swiftpack.api.TransformContext
import co.touchlab.swiftpack.spec.module.ApiTransform

internal interface InternalTransformContext: TransformContext {
    val transforms: List<ApiTransform>
}
