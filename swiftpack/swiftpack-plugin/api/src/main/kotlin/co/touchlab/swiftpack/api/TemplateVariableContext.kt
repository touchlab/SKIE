package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeReference
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec

interface TemplateVariableContext {
    fun KotlinTypeReference.Id.swiftTemplateVariable(): DeclaredTypeName

    // TODO: Add support for "top-level" Swift types (e.g. String, Int8, Int16, etc.)
    fun KotlinTypeReference<*>.swiftTemplateVariable(): DeclaredTypeName {
        return id.swiftTemplateVariable()
    }

    fun KotlinPropertyReference.Id.swiftTemplateVariable(): PropertySpec

    fun KotlinPropertyReference.swiftTemplateVariable(): PropertySpec {
        return id.swiftTemplateVariable()
    }

    fun KotlinFunctionReference.Id.swiftTemplateVariable(): FunctionSpec

    fun KotlinFunctionReference.swiftTemplateVariable(): FunctionSpec {
        return id.swiftTemplateVariable()
    }

    fun KotlinEnumEntryReference.Id.swiftTemplateVariable(): PropertySpec

    fun KotlinEnumEntryReference.swiftTemplateVariable(): PropertySpec {
        return id.swiftTemplateVariable()
    }
}
