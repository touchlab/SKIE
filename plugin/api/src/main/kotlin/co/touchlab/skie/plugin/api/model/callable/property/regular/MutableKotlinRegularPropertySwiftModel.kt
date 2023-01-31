package co.touchlab.skie.plugin.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.MutableKotlinPropertySwiftModel

interface MutableKotlinRegularPropertySwiftModel : KotlinRegularPropertySwiftModel, MutableKotlinPropertySwiftModel,
    MutableKotlinDirectlyCallableMemberSwiftModel
