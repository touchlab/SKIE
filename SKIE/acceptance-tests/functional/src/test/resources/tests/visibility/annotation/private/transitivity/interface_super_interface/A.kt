package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`interface_super_interface`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
interface A

@SkieVisibility.Internal
interface B : A
