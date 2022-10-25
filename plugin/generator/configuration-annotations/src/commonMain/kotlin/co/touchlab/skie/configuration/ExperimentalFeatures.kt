package co.touchlab.skie.configuration

object ExperimentalFeatures {

    /**
     * Enables experimental features for the annotated declaration.
     * Experimental features might not be fully implemented yet which may cause compilation problems.
     * Has effect only if the experimental features are globally disabled.
     */
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    /**
     * Disables experimental features for the annotated declaration.
     */
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled
}
