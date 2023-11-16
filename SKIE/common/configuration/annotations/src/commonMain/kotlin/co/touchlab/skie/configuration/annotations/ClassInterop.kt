package co.touchlab.skie.configuration.annotations

@Target
annotation class ClassInterop {

    /**
     * See [co.touchlab.skie.configuration.ClassInterop.StableTypeAlias]
     */
    @Target
    annotation class StableTypeAlias {

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Enabled

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Disabled
    }
}
