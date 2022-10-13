package co.touchlab.swiftpack.api.internal

import co.touchlab.swiftpack.api.TemplateVariableContext
import co.touchlab.swiftpack.spec.module.SwiftTemplateVariable

internal interface InternalTemplateVariableContext: TemplateVariableContext {
    val variables: Collection<SwiftTemplateVariable<*>>
}
