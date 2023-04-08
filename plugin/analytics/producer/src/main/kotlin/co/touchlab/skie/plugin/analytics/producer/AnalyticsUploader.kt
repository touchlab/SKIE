package co.touchlab.skie.plugin.analytics.producer

import java.net.URLConnection
import java.nio.file.Path

object AnalyticsUploader {

    fun uploadAllIfPossible(directory: Path) {
        try {
//            URLConnection.set
        } catch (_: Throwable) {
            // Log to bugsnag
        }
    }
}
