package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeReference
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec

interface TemplateVariableContext {
    fun KotlinTypeReference.Id.templateVariable(): DeclaredTypeName

    // TODO: Add support for "top-level" Swift types (e.g. String, Int8, Int16, etc.)
    fun KotlinTypeReference<*>.templateVariable(): DeclaredTypeName {
        return id.templateVariable()
    }

    fun KotlinPropertyReference.Id.templateVariable(): PropertySpec

    fun KotlinPropertyReference.templateVariable(): PropertySpec {
        return id.templateVariable()
    }

    fun KotlinFunctionReference.Id.templateVariable(): FunctionSpec

    fun KotlinFunctionReference.templateVariable(): FunctionSpec {
        return id.templateVariable()
    }

    fun KotlinEnumEntryReference.Id.templateVariable(): PropertySpec

    fun KotlinEnumEntryReference.templateVariable(): PropertySpec {
        return id.templateVariable()
    }
}
