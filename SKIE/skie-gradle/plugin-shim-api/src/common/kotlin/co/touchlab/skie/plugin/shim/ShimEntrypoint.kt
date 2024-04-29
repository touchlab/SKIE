package co.touchlab.skie.plugin.shim

interface ShimEntrypoint {

    val distributionProvider: DistributionProvider

    val nativeCompilerDownloaderProvider: NativeCompilerDownloaderProvider

    val launchScheduler: LaunchScheduler
}
