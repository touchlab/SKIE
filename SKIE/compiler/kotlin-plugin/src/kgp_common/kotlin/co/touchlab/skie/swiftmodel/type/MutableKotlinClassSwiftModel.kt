package co.touchlab.skie.swiftmodel.type

interface MutableKotlinClassSwiftModel : KotlinClassSwiftModel, MutableKotlinTypeSwiftModel {

    override val companionObject: MutableKotlinClassSwiftModel?

    override val nestedClasses: List<MutableKotlinClassSwiftModel>
}
