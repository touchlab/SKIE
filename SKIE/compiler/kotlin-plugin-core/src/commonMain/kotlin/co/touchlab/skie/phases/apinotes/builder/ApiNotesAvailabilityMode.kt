package co.touchlab.skie.phases.apinotes.builder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ApiNotesAvailabilityMode {

    @SerialName("available")
    Available,

    @SerialName("OSX")
    OSX,

    @SerialName("iOS")
    IOS,

    @SerialName("none")
    None,

    @SerialName("nonswift")
    NonSwift,
}
