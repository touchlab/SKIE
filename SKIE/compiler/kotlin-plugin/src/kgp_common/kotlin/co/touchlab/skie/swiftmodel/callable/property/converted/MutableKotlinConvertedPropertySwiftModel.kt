package co.touchlab.skie.swiftmodel.callable.property.converted

import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.MutableKotlinPropertySwiftModel

interface MutableKotlinConvertedPropertySwiftModel : KotlinConvertedPropertySwiftModel, MutableKotlinPropertySwiftModel {

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = accessors

    override val getter: MutableKotlinFunctionSwiftModel

    override val setter: MutableKotlinFunctionSwiftModel?

    override val accessors: List<MutableKotlinFunctionSwiftModel>
        get() = listOfNotNull(getter, setter)
}
