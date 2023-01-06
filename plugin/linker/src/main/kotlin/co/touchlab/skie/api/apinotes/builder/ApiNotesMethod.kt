package co.touchlab.skie.api.apinotes.builder

import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

class ApiNotesMethod(
    private val objCSelector: String,
    private val kind: ApiNotesTypeMemberKind,
    private val swiftName: String?,
    private val isHidden: Boolean,
    private val isRemoved: Boolean,
) {

    context(SmartStringBuilder)
    fun append() {
        +"- Selector: \"${objCSelector}\""
        indented {
            +"MethodKind: ${kind.apiNotesRepresentation}"
            swiftName?.let { +"SwiftName: \"$it\"" }
            isHidden.ifTrue { +"SwiftPrivate: true" }
            isRemoved.ifTrue { +"Availability: nonswift" }
        }
    }
}
