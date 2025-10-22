package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.util.swift.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName

sealed class SkieErrorSirType(
    val objCName: String,
) : NonNullSirType() {

    abstract val headerCommentLines: List<String>

    abstract val errorMessage: String

    override val isHashable: Boolean = true

    override val isReference: Boolean = true

    private val evaluatedSirType by lazy {
        EvaluatedSirType.Eager(
            type = this,
            canonicalName = objCName,
            swiftPoetTypeName = DeclaredTypeName.qualifiedLocalTypeName(objCName),
            visibilityConstraint = SirVisibility.Public,
            referencedTypeDeclarations = emptySet(),
        )
    }

    override fun evaluate(): EvaluatedSirType = evaluatedSirType

    override fun inlineTypeAliases(): SirType =
        this

    // To ensure this type is never erased
    override fun asHashableType(): SirType? =
        this

    override fun asReferenceType(): SirType? =
        this

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): SkieErrorSirType =
        this

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SkieErrorSirType =
        this

    object Lambda : SkieErrorSirType("__SkieLambdaErrorType") {

        override val headerCommentLines: List<String> = listOf(
            "// Due to an Obj-C/Swift interop limitation, SKIE cannot generate Swift types with a lambda type argument.",
            "// Example of such type is: A<() -> Unit> where A<T> is a generic class.",
            "// To avoid compilation errors SKIE replaces these type arguments with __SkieLambdaErrorType, resulting in A<__SkieLambdaErrorType>.",
            "// Generated declarations that reference __SkieLambdaErrorType cannot be called in any way and the __SkieLambdaErrorType class cannot be used.",
            "// The original declarations can still be used in the same way as other declarations hidden by SKIE (and with the same limitations as without SKIE).",
        )

        override val errorMessage: String =
            "Due to an Obj-C/Swift interop limitation, SKIE cannot generate Swift types with a lambda type argument. " +
                "Example of such type is: A<() -> Unit> where A<T> is a generic class. " +
                "The original declarations can still be used in the same way as other declarations hidden by SKIE (and with the same limitations as without SKIE)."
    }

    data class UnknownCInteropFramework(val replacedTypeKotlinName: String) : SkieErrorSirType("__SkieUnknownCInteropFrameworkErrorType") {

        override val headerCommentLines: List<String> = listOf(
            "// Due to an Obj-C/Swift interop limitation, SKIE cannot generate Swift code that uses external Obj-C types for which SKIE doesn't know a fully qualified name.",
            "// This problem occurs when custom Cinterop bindings are used because those do not contain the name of the Framework that provides implementation for those binding.",
            "// The name can be configured manually using the SKIE Gradle configuration key 'ClassInterop.CInteropFrameworkName' in the same way as other SKIE features.",
            "// To avoid compilation errors SKIE replaces types with unknown Framework name with __SkieUnknownCInteropFrameworkErrorType.",
            "// Generated declarations that reference __SkieUnknownCInteropFrameworkErrorType cannot be called in any way and the __SkieUnknownCInteropFrameworkErrorType class cannot be used.",
        )

        override val errorMessage: String =
            "Unknown Swift framework for type '$replacedTypeKotlinName'. " +
                "This problem occurs when custom Cinterop bindings are used because those do not contain the name of the Framework that provides implementation for those binding. " +
                "The name can be configured manually using the SKIE Gradle configuration key 'ClassInterop.CInteropFrameworkName' in the same way as other SKIE features."
    }
}
