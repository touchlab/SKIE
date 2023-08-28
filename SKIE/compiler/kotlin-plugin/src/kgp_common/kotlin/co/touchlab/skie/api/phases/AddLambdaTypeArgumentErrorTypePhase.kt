package co.touchlab.skie.api.phases

import co.touchlab.skie.api.phases.header.BaseHeaderInsertionPhase
import co.touchlab.skie.api.phases.util.ObjCTypeRenderer
import java.io.File

class AddLambdaTypeArgumentErrorTypePhase(
    headerFile: File,
) : BaseHeaderInsertionPhase(headerFile) {

    override val insertedContent: List<String>
        get() =
            listOf(
                "// Due to an Obj-C/Swift interop limitation, SKIE cannot generate Swift types with a lambda type argument.",
                "// Example of such type is: A<() -> Unit> where A<T> is a generic class.",
                "// To avoid compilation errors SKIE replaces these type arguments with __SkieLambdaErrorType, resulting in A<__SkieLambdaErrorType>.",
                "// Generated declarations that reference __SkieLambdaErrorType cannot be called in any way and the __SkieLambdaErrorType class cannot be used.",
                "// The original declarations can still be used in the same way as other declarations hidden by SKIE (and with the same limitations as without SKIE).",
                "@interface __SkieLambdaErrorType : NSObject",
                "- (instancetype _Nonnull)init __attribute__((unavailable));",
                "+ (instancetype _Nonnull)new __attribute__((unavailable));",
                "@end",
            )

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}
