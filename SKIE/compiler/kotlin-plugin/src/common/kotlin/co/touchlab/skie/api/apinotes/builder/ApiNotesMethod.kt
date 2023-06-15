package co.touchlab.skie.api.apinotes.builder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiNotesMethod(
    @SerialName("Selector")
    val objCSelector: String,
    @SerialName("MethodKind")
    val kind: ApiNotesTypeMemberKind,
    @SerialName("SwiftName")
    val swiftName: String? = null,
    @SerialName("SwiftPrivate")
    val isHidden: Boolean = false,
    @SerialName("Availability")
    val availability: ApiNotesAvailabilityMode = ApiNotesAvailabilityMode.Available,
    @SerialName("AvailabilityMsg")
    val availabilityMessage: String = "",
    @SerialName("ResultType")
    val resultType: String = "",
    @SerialName("Parameters")
    val parameters: List<ApiNotesParameter> = emptyList(),
)
