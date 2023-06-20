package co.touchlab.skie.api.apinotes.builder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiNotesProperty(
    @SerialName("Name")
    val objCName: String,
    @SerialName("PropertyKind")
    val kind: ApiNotesTypeMemberKind? = null,
    @SerialName("SwiftName")
    val swiftName: String? = null,
    @SerialName("SwiftPrivate")
    val isHidden: Boolean = false,
    @SerialName("Availability")
    val availability: ApiNotesAvailabilityMode = ApiNotesAvailabilityMode.Available,
    @SerialName("AvailabilityMsg")
    val availabilityMessage: String = "",
    @SerialName("Type")
    val type: String = ""
)
