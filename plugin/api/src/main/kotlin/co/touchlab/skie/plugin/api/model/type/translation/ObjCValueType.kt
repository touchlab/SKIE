package co.touchlab.skie.plugin.api.model.type.translation

import org.jetbrains.kotlin.backend.konan.llvm.LlvmParameterAttribute

enum class ObjCValueType(val encoding: String, val defaultParameterAttributes: List<LlvmParameterAttribute> = emptyList()) {
    BOOL("c", listOf(LlvmParameterAttribute.SignExt)),
    UNICHAR("S", listOf(LlvmParameterAttribute.ZeroExt)),
    // TODO: Switch to explicit SIGNED_CHAR
    CHAR("c", listOf(LlvmParameterAttribute.SignExt)),
    SHORT("s", listOf(LlvmParameterAttribute.SignExt)),
    INT("i"),
    LONG_LONG("q"),
    UNSIGNED_CHAR("C", listOf(LlvmParameterAttribute.ZeroExt)),
    UNSIGNED_SHORT("S", listOf(LlvmParameterAttribute.ZeroExt)),
    UNSIGNED_INT("I"),
    UNSIGNED_LONG_LONG("Q"),
    FLOAT("f"),
    DOUBLE("d"),
    POINTER("^v")
}
