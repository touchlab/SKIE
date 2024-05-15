package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.SkieVisibility
import kotlin.reflect.KClass

/**
 * Configures visibility of the exported Kotlin declarations in Swift.
 * In the future, the visibility will be applied directly to the Obj-C header meaning it will also affect external Obj-C code.
 *
 * Warning: SKIE does not check whether the configured visibility is correct or not.
 * For example, it's possible to create a public interface with internal members.
 * Invalid configurations will likely lead to weird issues and compiler crashes.
 * In the future, SKIE might introduce explicit checks that will crash the compilation explicitly in those cases.
 */
object SkieVisibility : ConfigurationKey.Enum<co.touchlab.skie.configuration.SkieVisibility.Level>, ConfigurationScope.All {

    enum class Level(internal val associatedAnnotation: KClass<out Annotation>) {
        /**
         * The declaration will be visible from external modules as usual.
         */
        Public(SkieVisibility.Public::class),

        /**
         * The declaration will be visible from external modules, but it will be marked as `swift-private`.
         * (Xcode will not include it in autocomplete suggestions.)
         */
        PublicButHidden(SkieVisibility.PublicButHidden::class),

        /**
         * The declaration will be visible from external modules, but it will be:
         *  - marked as `swift-private` (Xcode will not include it in autocomplete suggestions.),
         *  - renamed in Swift by adding the `__` prefix (Obj-C name remains the same); constructors are not affected.
         */
        PublicButReplaced(SkieVisibility.PublicButReplaced::class),

        /**
         * The declaration will be visible only for declarations within the Kotlin module (including custom Swift code bundled by SKIE).
         */
        Internal(SkieVisibility.Internal::class),

        /**
         * The declaration will be visible only for declarations within the Kotlin module (including custom Swift code bundled by SKIE).
         * Additionally, the declaration will be renamed in Swift by adding the `__` prefix (Obj-C name remains the same); constructors are not affected.
         */
        InternalAndReplaced(SkieVisibility.InternalAndReplaced::class),

        /**
         * The declaration will either be Public or Internal.
         * Which one is chosen depends on whether the declaration is automatically wrapped by SKIE or not.
         *
         * For example, a top-level function originally exposed as `FileKt.functionName` will be internal, if SKIE generated the global function wrapper for it.
         */
        InternalIfWrapped(SkieVisibility.InternalIfWrapped::class),

        /**
         * The declaration will not be visible.
         *
         * Note that this doesn't change whether the declaration is exported by the Kotlin compiler, therefore the compilation overhead from the exported declaration remains unchanged.
         */
        Private(SkieVisibility.Private::class),
    }

    override val defaultValue: Level = Level.Public

    override fun valueOf(value: String): Level = Level.valueOf(value)

    override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Level? =
        Level.values().firstOrNull { configurationTarget.hasAnnotation(it.associatedAnnotation) }
}
