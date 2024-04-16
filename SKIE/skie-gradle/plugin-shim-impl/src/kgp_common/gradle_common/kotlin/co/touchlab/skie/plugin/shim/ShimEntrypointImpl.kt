package co.touchlab.skie.plugin.shim

// Constructed through reflection in SKIE Gradle Plugin.
@Suppress("unused")
class ShimEntrypointImpl: ShimEntrypoint {
    override val distributionProvider = DistributionProviderImpl
    override val nativeCompilerDownloaderProvider = NativeCompilerDownloaderProviderImpl
    override val launchScheduler = LaunchScheduler()
}
