package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfigurationTarget
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.crash.BugsnagFactory
import co.touchlab.skie.plugin.analytics.producer.compressor.AnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import co.touchlab.skie.util.Environment
import co.touchlab.skie.util.directory.SkieAnalyticsDirectories
import com.bugsnag.Bugsnag
import kotlin.io.path.writeBytes
import kotlin.reflect.KClass

class AnalyticsCollector(
    analyticsDirectories: SkieAnalyticsDirectories,
    private val buildId: String,
    private val skieVersion: String,
    type: BugsnagFactory.Type,
    private val configuration: AnalyticsConfiguration,
) : AnalyticsConfigurationTarget<AnalyticsFeature.CrashReporting> {

    override val featureType: KClass<AnalyticsFeature.CrashReporting> = AnalyticsFeature.CrashReporting::class

    private val bugsnag: Bugsnag =
        BugsnagFactory.create(skieVersion, type, Environment.current)

    private val analyticsDirectories = analyticsDirectories.forEnvironment(Environment.current).map { it.toPath() }

    @Synchronized
    fun collect(producers: List<AnalyticsProducer<*>>) {
        producers.parallelStream().forEach {
            collectSafely(it)
        }
    }

    @Synchronized
    fun collect(vararg producers: AnalyticsProducer<*>) {
        collect(producers.toList())
    }

    private fun collectSafely(producer: AnalyticsProducer<*>) {
        try {
            produceAndCollectData(producer)
        } catch (e: Throwable) {
            reportAnalyticsError(producer.name, e)
        }
    }

    private fun produceAndCollectData(producer: AnalyticsProducer<*>) {
        val feature = configuration.getFeature(producer)

        if (feature?.isEnabled == true) {
            @Suppress("UNCHECKED_CAST")
            val analyticsResult = (producer as AnalyticsProducer<AnalyticsFeature>).produce(feature)

            collectData(producer.name, analyticsResult)
        }
    }

    private fun collectData(analyticsType: String, data: ByteArray) {
        collectUsingDeflate(analyticsType, data)
        collectUsingBZip2(analyticsType, data)
    }

    private fun collectUsingDeflate(analyticsType: String, analyticsResult: ByteArray) {
        collectUsingCompressor(analyticsType, analyticsResult, FastAnalyticsCompressor)
    }

    private fun collectUsingBZip2(analyticsType: String, analyticsResult: ByteArray) {
        val betterCompressionTask = Thread {
            try {
                collectUsingCompressor(analyticsType, analyticsResult, EfficientAnalyticsCompressor)
            } catch (e: Throwable) {
                reportAnalyticsError("$analyticsType-efficient-compressor", e)
            }
        }

        betterCompressionTask.start()
    }

    private fun collectUsingCompressor(
        analyticsType: String,
        analyticsResult: ByteArray,
        compressor: AnalyticsCompressor,
    ) {
        val artifact = AnalyticsArtifact(
            buildId = buildId,
            type = analyticsType,
            skieVersion = skieVersion,
            environment = Environment.current,
            compressionMethod = compressor.method,
        )

        val compressedData = compressor.compress(analyticsResult)

        val encryptedData = encrypt(compressedData)

        analyticsDirectories
            .map { artifact.path(it) }
            .forEach {
                it.writeBytes(encryptedData)
            }
    }

    private fun encrypt(data: ByteArray): ByteArray =
        AnalyticsEncryptor.encrypt(data)

    private fun reportAnalyticsError(name: String, error: Throwable) {
        val wrappingException = SkieAnalyticsError(buildId, name, error)

        sendExceptionLogIfEnabled(name, wrappingException)

        rethrowIfNotProduction(error)
    }

    @Synchronized
    fun logException(exception: Throwable) {
        val name = exception.stackTraceToString().hashCode().toString()
        val wrappingException = SkieThrowable(buildId, exception)

        sendExceptionLogIfEnabled(name, wrappingException)
    }

    @Synchronized
    fun logExceptionAndRethrowIfNotProduction(exception: Throwable) {
        logException(exception)

        rethrowIfNotProduction(exception)
    }

    @Synchronized
    fun logException(exception: String) {
        val name = exception.hashCode().toString()
        val wrappingException = SkieThrowable(buildId, exception)

        sendExceptionLogIfEnabled(name, wrappingException)
    }

    private fun sendExceptionLogIfEnabled(name: String, exception: Throwable) {
        val crashReportingConfiguration = configuration.getFeature(this)

        if (crashReportingConfiguration?.isEnabled == true) {
            sendExceptionLog(name, exception)
        }
    }

    private fun sendExceptionLog(name: String, exception: Throwable) {
        val logName = "exception-$name"

        bugsnag.notify(exception)

        try {
            val encodedException = exception.stackTraceToString().toByteArray()

            collectData(logName, encodedException)
        } catch (e: Throwable) {
            bugsnag.notify(e)

            rethrowIfNotProduction(e)
        }
    }

    private fun rethrowIfNotProduction(exception: Throwable) {
        if (!Environment.current.canBeProduction()) {
            throw exception
        }
    }

    private class SkieThrowable(
        buildId: String,
        throwableDescription: String,
    ) : Throwable("Exception in build with id: $buildId.\n$throwableDescription") {

        constructor(buildId: String, throwable: Throwable) : this(buildId, throwable.stackTraceToString())
    }

    private class SkieAnalyticsError(
        buildId: String,
        name: String,
        throwable: Throwable,
    ) : Throwable("SKIE analytics error in \"$name\", in build with id: $buildId.\n${throwable.stackTraceToString()}")
}
