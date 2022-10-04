package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.symbol.KotlinEnumEntry
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty
import co.touchlab.swiftpack.spec.symbol.KotlinType
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec

interface TemplateVariableContext {
    fun KotlinType.Id.templateVariable(): DeclaredTypeName

    // TODO: Add support for "top-level" Swift types (e.g. String, Int8, Int16, etc.)
    fun KotlinType<*>.templateVariable(): DeclaredTypeName {
        return id.templateVariable()
    }

    fun KotlinProperty.Id.templateVariable(): PropertySpec

    fun KotlinProperty.templateVariable(): PropertySpec {
        return id.templateVariable()
    }

    fun KotlinFunction.Id.templateVariable(): FunctionSpec

    fun KotlinFunction.templateVariable(): FunctionSpec {
        return id.templateVariable()
    }

    fun KotlinEnumEntry.Id.templateVariable(): PropertySpec

    fun KotlinEnumEntry.templateVariable(): PropertySpec {
        return id.templateVariable()
    }
}
