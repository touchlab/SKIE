package co.touchlab.skie.util

import co.touchlab.skie.kir.element.KirElement
import java.util.Collections

class Reporter {

    private val mutableReports = Collections.synchronizedList(mutableListOf<Report>())

    val reports: List<Report> by ::mutableReports

    fun report(severity: Severity, message: String, source: KirElement? = null) {
        mutableReports.add(Report(message, severity, source))
    }

    fun error(message: String, source: KirElement? = null) {
        report(Severity.Error, message, source)
    }

    fun warning(message: String, source: KirElement? = null) {
        report(Severity.Warning, message, source)
    }

    enum class Severity {
        Error, Warning
    }

    data class Report(val message: String, val severity: Severity, val source: KirElement?)
}
