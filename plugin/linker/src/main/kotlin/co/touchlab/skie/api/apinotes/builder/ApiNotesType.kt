package co.touchlab.skie.api.apinotes.builder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiNotesType(
    @SerialName("Name")
    val objCFqName: String,
    @SerialName("SwiftBridge")
    val bridgeFqName: String? = null,
    @SerialName("SwiftName")
    val swiftFqName: String = "",
    @SerialName("SwiftPrivate")
    val isHidden: Boolean = false,
    @SerialName("Availability")
    val availability: ApiNotesAvailabilityMode = ApiNotesAvailabilityMode.Available,
    @SerialName("AvailabilityMsg")
    val availabilityMessage: String = "",
    @SerialName("SwiftImportAsNonGeneric")
    val importAsNonGeneric: Boolean = false,
    @SerialName("Properties")
    val properties: List<ApiNotesProperty> = emptyList(),
    @SerialName("Methods")
    val methods: List<ApiNotesMethod> = emptyList(),
)
