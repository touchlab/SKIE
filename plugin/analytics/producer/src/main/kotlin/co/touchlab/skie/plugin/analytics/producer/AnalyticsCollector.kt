package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfigurationTarget
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.crash.BugsnagFactory
import co.touchlab.skie.plugin.analytics.producer.compressor.AnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import co.touchlab.skie.plugin.license.SkieLicense
import com.bugsnag.Bugsnag
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.writeBytes
import kotlin.reflect.KClass

class AnalyticsCollector(
    private val analyticsDirectory: Path,
    private val buildId: String,
    skieVersion: String,
    type: BugsnagFactory.Type,
    private val license: SkieLicense,
    private val configuration: AnalyticsConfiguration,
) : AnalyticsConfigurationTarget<AnalyticsFeature.CrashReporting> {

    override val featureType: KClass<AnalyticsFeature.CrashReporting> = AnalyticsFeature.CrashReporting::class

    private val bugsnag: Bugsnag =
        BugsnagFactory.create(skieVersion, type, license.environment)

    private val backgroundTasks = CopyOnWriteArrayList<Thread>()

    private val environmentPrefix = license.environment.name

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

    private fun collectData(name: String, data: ByteArray) {
        collectUsingDeflate(name, data)
        collectUsingBZip2(name, data)
    }

    private fun collectUsingDeflate(name: String, analyticsResult: ByteArray) {
        collectUsingCompressor(name, analyticsResult, FastAnalyticsCompressor, 1)
    }

    private fun collectUsingBZip2(name: String, analyticsResult: ByteArray) {
        val betterCompressionTask = Thread {
            try {
                collectUsingCompressor(name, analyticsResult, EfficientAnalyticsCompressor, 2)
            } catch (e: Throwable) {
                reportAnalyticsError("$name-2", e)
            }
        }

        betterCompressionTask.start()

        backgroundTasks.add(betterCompressionTask)
    }

    private fun collectUsingCompressor(
        name: String,
        analyticsResult: ByteArray,
        compressor: AnalyticsCompressor,
        fileVersion: Int,
    ) {
        val fileName = FileWithMultipleVersions.addVersion("$environmentPrefix.$buildId.$name", fileVersion)
        val file = analyticsDirectory.resolve(fileName)

        val compressedData = compressor.compress(analyticsResult)

        val encryptedData = encrypt(compressedData)

        file.writeBytes(encryptedData)
    }

    private fun encrypt(data: ByteArray): ByteArray =
        AnalyticsEncryptor.encrypt(data)

    private fun reportAnalyticsError(name: String, error: Throwable) {
        val wrappingException = SkieAnalyticsError(buildId, name, error)

        sendExceptionLogIfEnabled(name, wrappingException)

        rethrowIfDev(error)
    }

    @Synchronized
    fun logException(exception: Throwable) {
        val name = exception.stackTraceToString().hashCode().toString()
        val wrappingException = SkieThrowable(buildId, exception)

        sendExceptionLogIfEnabled(name, wrappingException)
    }

    @Synchronized
    fun logExceptionAndRethrowIfDev(exception: Throwable) {
        logException(exception)

        rethrowIfDev(exception)
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

            rethrowIfDev(e)
        }
    }

    private fun rethrowIfDev(exception: Throwable) {
        if (license.environment == SkieLicense.Environment.Dev) {
            throw exception
        }
    }

    @Synchronized
    fun waitForBackgroundTasks() {
        backgroundTasks.forEach {
            it.join()
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
