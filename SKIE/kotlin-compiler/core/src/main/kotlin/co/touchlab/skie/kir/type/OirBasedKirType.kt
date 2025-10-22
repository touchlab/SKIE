package co.touchlab.skie.kir.type

import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.PointerOirType
import co.touchlab.skie.oir.type.PrimitiveOirType
import co.touchlab.skie.oir.type.VoidOirType

data class OirBasedKirType(val oirType: OirType) : KirType() {

    init {
        require(oirType is VoidOirType || oirType is PointerOirType || oirType is PrimitiveOirType) {
            "Unsupported OirType: $oirType"
        }
    }
}
