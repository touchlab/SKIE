package co.touchlab.skie.swiftmodel.callable.property.converted

import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.KotlinPropertySwiftModel

interface KotlinConvertedPropertySwiftModel : KotlinPropertySwiftModel {

    override val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>
        get() = accessors

    val getter: KotlinFunctionSwiftModel

    val setter: KotlinFunctionSwiftModel?

    val accessors: List<KotlinFunctionSwiftModel>
        get() = listOfNotNull(getter, setter)
}
