package co.touchlab.swiftgen.configuration

class SwiftGenConfiguration : ConfigurationDeclaration() {

    val sealedInteropDefaults = SealedInteropDefaults()

    inner class SealedInteropDefaults {

        val enabled by option(
            name = "sealed-enabled",
            defaultValue = true,
            description = "If true, the interop code is generated for all sealed classes/interfaces " +
                    "for which the plugin is not explicitly disabled via an annotation. " +
                    "Otherwise, the code is generated only for explicitly annotated sealed classes/interfaces.",
            valueDescription = "<true|false>, defaults to 'true'"
        )

        val functionName by option(
            name = "sealed-functionName",
            defaultValue = "exhaustively",
            description = "The default name for the function used inside `switch`.",
            valueDescription = "any valid Swift identifier, defaults to 'exhaustively'"
        )

        val elseName by option(
            name = "sealed-elseName",
            defaultValue = "Else",
            description = "The default name for the custom `else` case that is generated " +
                    "if some children are hidden / not accessible from Swift.",
            valueDescription = "any valid Swift identifier, defaults to 'Else'"
        )

        val visibleCases by option(
            name = "sealed-visibleCases",
            defaultValue = true,
            description = "If true the enum cases are generated for all direct children of " +
                    "sealed class/interface that are visible from Swift." +
                    "This behavior can be overridden on a case by case bases by an annotation. " +
                    "If false, each child must be explicitly annotated or it will be considered as hidden.",
            valueDescription = "<true|false>, defaults to 'true'"
        )
    }
}
