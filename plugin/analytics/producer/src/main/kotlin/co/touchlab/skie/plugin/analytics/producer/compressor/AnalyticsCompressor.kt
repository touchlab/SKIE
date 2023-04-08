package co.touchlab.skie.plugin.analytics.producer.compressor

interface AnalyticsCompressor {

    fun compress(data: ByteArray): ByteArray

    fun decompress(data: ByteArray): ByteArray
}
