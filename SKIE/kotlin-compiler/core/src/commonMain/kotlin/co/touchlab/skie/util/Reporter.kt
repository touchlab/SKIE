package co.touchlab.skie.util

import java.util.Collections

abstract class Reporter<T> {

    private val mutableReports = Collections.synchronizedList(mutableListOf<Report<T>>())

    val reports: List<Report<T>> by ::mutableReports

    fun report(severity: Severity, message: String, source: T? = null) {
        mutableReports.add(Report(message, severity, source))
    }

    fun error(message: String, source: T? = null) {
        report(Severity.Error, message, source)
    }

    fun warning(message: String, source: T? = null) {
        report(Severity.Warning, message, source)
    }

    enum class Severity {
        Error, Warning
    }

    data class Report<T>(val message: String, val severity: Severity, val source: T?)
}
