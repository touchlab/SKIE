package co.touchlab.skie.api.apinotes.builder

import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

class ApiNotesProperty(
    private val objCName: String,
    private val kind: ApiNotesTypeMemberKind,
    private val swiftName: String?,
    private val isHidden: Boolean,
    private val isRemoved: Boolean,
) {

    context(SmartStringBuilder)
    fun append() {
        +"- Name: $objCName"

        indented {
            +"PropertyKind: ${kind.apiNotesRepresentation}"
            swiftName?.let { +"SwiftName: $it" }
            isHidden.ifTrue { +"SwiftPrivate: true" }
            isRemoved.ifTrue { +"Availability: nonswift" }
        }
    }
}
