package co.touchlab.skie.api.model.function

import co.touchlab.skie.api.model.parameter.OriginalKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

abstract class BaseKotlinFunctionSwiftModel(
    final override val descriptor: FunctionDescriptor,
    override val receiver: KotlinTypeSwiftModel,
    namer: ObjCExportNamer,
) : KotlinFunctionSwiftModel {

    override val identifier: String
        get() = originalIdentifier

    override val parameters: List<KotlinParameterSwiftModel>
        get() = originalParameters

    override val visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val objCSelector: String = namer.getSelector(descriptor)

    private val originalIdentifier: String

    private val originalParameters: List<KotlinParameterSwiftModel>

    init {
        val swiftName = namer.getSwiftName(descriptor)

        val (identifier, argumentLabelsString) = "(.+?)\\((.*?)\\)".toRegex().matchEntire(swiftName)?.destructured
            ?: error("Unable to parse swift name: $swiftName")

        val argumentLabels = argumentLabelsString.split(":").map { it.trim() }.filter { it.isNotEmpty() }

        originalIdentifier = identifier
        originalParameters = argumentLabels.map { OriginalKotlinParameterSwiftModel(it) }
    }
}
