package co.touchlab.skie.configuration.annotations

/**
 * Configures visibility of the exported Kotlin declarations in Swift.
 * In the future, the visibility will be applied directly to the Obj-C header meaning it will also affect external Obj-C code.
 */
@Target
annotation class SkieVisibility {

    /**
     * The declaration will be visible from external modules as usual.
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
    annotation class Public

    /**
     * The declaration will be visible from external modules, but it will be marked as `swift-private`.
     * (Xcode will not include it in autocomplete suggestions.)
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
    annotation class PublicButHidden

    /**
     * The declaration will be visible from external modules, but it will be:
     *  - marked as `swift-private` (Xcode will not include it in autocomplete suggestions.),
     *  - renamed in Swift by adding the `__` prefix (Obj-C name remains the same)
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
    annotation class PublicButReplaced

    /**
     * The declaration will be visible only for declarations within the Kotlin module (including custom Swift code bundled by SKIE).
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
    annotation class Internal

    /**
     * The declaration will be visible only for declarations within the Kotlin module (including custom Swift code bundled by SKIE).
     * Additionally, the declaration will be renamed in Swift by adding the `__` prefix (Obj-C name remains the same)
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
    annotation class InternalAndReplaced

    /**
     * The callable declaration will either be Public or Internal.
     * Which one is chosen depends on whether the declaration is automatically wrapped by SKIE or not.
     *
     * For example, a top-level function originally exposed as `FileKt.functionName` will be internal, if SKIE generated the global function wrapper for it.
     *
     * Note that this setting will only affect non-abstract callable declarations (functions, properties, constructors) - not classes.
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
    annotation class InternalIfWrapped

    /**
     * The declaration will not be visible.
     *
     * Note that this doesn't change whether the declaration is exported by the Kotlin compiler, therefore the compilation overhead from the exported declaration remains unchanged.
     */
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
    annotation class Private
}

