package co.touchlab.skie.plugin.api.model.type.bridge

sealed class MethodBridgeParameter {

    sealed class Receiver : MethodBridgeParameter() {
        object Static : Receiver()
        object Factory : Receiver()
        object Instance : Receiver()
    }

    object Selector : MethodBridgeParameter()

    sealed class ValueParameter : MethodBridgeParameter() {
        data class Mapped(val bridge: NativeTypeBridge) : ValueParameter()
        object ErrorOutParameter : ValueParameter()
        data class SuspendCompletion(val useUnitCompletion: Boolean) : ValueParameter()
    }
}
