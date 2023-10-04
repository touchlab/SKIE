package co.touchlab.skie.swiftmodel.callable.property.regular

import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType

interface KotlinRegularPropertySwiftModel : KotlinPropertySwiftModel, KotlinDirectlyCallableMemberSwiftModel {

    val getter: KotlinRegularPropertyGetterSwiftModel

    val setter: KotlinRegularPropertySetterSwiftModel?

    val primarySirProperty: SirProperty
        get() = bridgedSirProperty ?: kotlinSirProperty

    val kotlinSirProperty: SirProperty

    val bridgedSirProperty: SirProperty?

    val objCType: ObjCType

    val objCName: String
}
