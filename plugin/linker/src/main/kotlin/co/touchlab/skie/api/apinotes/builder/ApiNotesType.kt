package co.touchlab.skie.api.apinotes.builder

import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

class ApiNotesType(
    private val objCFqName: String,
    private val bridgeFqName: String?,
    private val swiftFqName: String,
    private val isHidden: Boolean,
    private val isRemoved: Boolean,
    private val properties: List<ApiNotesProperty>,
    private val methods: List<ApiNotesMethod>,
) {

    context(SmartStringBuilder)
    fun appendApiNote() {
        +"- Name: \"$objCFqName\""

        indented {
            bridgeFqName?.let { +"SwiftBridge: $it" }
            swiftFqName.let { +"SwiftName: $it" }
            isHidden.ifTrue { +"SwiftPrivate: true" }
            isRemoved.ifTrue { +"Availability: nonswift" }

            if (properties.isNotEmpty()) {
                +"Properties:"
                properties.forEach {
                    it.append()
                }
            }

            if (methods.isNotEmpty()) {
                +"Methods:"
                methods.forEach {
                    it.append()
                }
            }
        }
    }
}
