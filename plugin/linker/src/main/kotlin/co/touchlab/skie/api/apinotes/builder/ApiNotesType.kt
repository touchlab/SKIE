package co.touchlab.skie.api.apinotes.builder

import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec
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

    private val flattenedBridgeFqName: String? = bridgeFqName?.replace(".", "__")

    private val needsTypeAliasForBridging: Boolean = bridgeFqName != flattenedBridgeFqName

    fun withoutBridging(): ApiNotesType =
        ApiNotesType(
            objCFqName = objCFqName,
            bridgeFqName = null,
            swiftFqName = swiftFqName,
            isHidden = isHidden,
            isRemoved = isRemoved,
            properties = properties,
            methods = methods,
        )

    context(SmartStringBuilder)
    fun appendApiNote() {
        +"- Name: \"$objCFqName\""

        indented {
            flattenedBridgeFqName?.let { +"SwiftBridge: $it" }
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

    context(FileSpec.Builder)
    fun appendTypeAliasForBridgingIfNeeded() {
        val bridgeFqName = bridgeFqName ?: return
        val flattenedBridgeFqName = flattenedBridgeFqName ?: return
        if (!needsTypeAliasForBridging) {
            return
        }

        addType(
            TypeAliasSpec.builder(flattenedBridgeFqName, DeclaredTypeName.qualifiedLocalTypeName(bridgeFqName))
                .addModifiers(Modifier.PUBLIC)
                .build()
        )
    }
}
