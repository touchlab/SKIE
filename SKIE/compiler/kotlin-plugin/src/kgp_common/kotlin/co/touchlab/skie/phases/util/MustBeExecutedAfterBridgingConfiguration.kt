package co.touchlab.skie.phases.util

// Phases with this annotation must be executed after the bridging configuration phase because they directly access the primarySirClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MustBeExecutedAfterBridgingConfiguration
