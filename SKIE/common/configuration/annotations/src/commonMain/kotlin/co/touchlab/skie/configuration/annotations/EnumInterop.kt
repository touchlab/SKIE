package co.touchlab.skie.configuration.annotations

@Target
annotation class EnumInterop {

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled

    /**
     * See [co.touchlab.skie.configuration.EnumInterop.LegacyCaseNames]
     */
    @Target
    annotation class LegacyCaseNames {

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Enabled

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Disabled
    }
}
