package tests.visibility.annotation.validation.multiple_annotations.function

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
@SkieVisibility.Private
fun foo() {
}
