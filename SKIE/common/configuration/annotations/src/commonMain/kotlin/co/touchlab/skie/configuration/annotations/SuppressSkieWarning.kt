package co.touchlab.skie.configuration.annotations

@Target
annotation class SuppressSkieWarning {

    /**
     * Suppresses a warning about SKIE renaming a declaration because of a name collision.
     */
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
    @Retention(AnnotationRetention.BINARY)
    annotation class NameCollision(val suppress: Boolean = true)
}
