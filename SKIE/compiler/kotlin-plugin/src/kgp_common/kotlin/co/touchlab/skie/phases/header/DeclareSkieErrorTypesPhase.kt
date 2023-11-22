package co.touchlab.skie.phases.header

import co.touchlab.skie.phases.header.util.BaseHeaderInsertionPhase
import co.touchlab.skie.sir.type.SkieErrorSirType

object DeclareSkieErrorTypesPhase : BaseHeaderInsertionPhase() {

    override val insertedContent: List<String>
        get() =
            listOf(
                SkieErrorSirType.Lambda,
                SkieErrorSirType.UnknownCInteropModule(""),
            )
                .flatMap {
                    it.headerCommentLines + listOf(
                        "@interface ${it.objCName} : NSObject",
                        "- (instancetype _Nonnull)init __attribute__((unavailable));",
                        "+ (instancetype _Nonnull)new __attribute__((unavailable));",
                        "@end",
                        "",
                    )
                }
                .dropLast(1)

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}
