package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.Reporter.Severity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.renderer.DescriptorRenderer

object ProcessReportedMessagesPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        reporter.reports.forEach {
            report(it)
        }
    }

    context(LinkPhase.Context)
    private fun report(report: Reporter.Report) {
        val declaration = report.source?.let { descriptorKirProvider.findDeclarationDescriptor(it) }

        val location = MessageUtil.psiElementToMessageLocation(declaration?.findPsi())?.let {
            CompilerMessageLocation.create(it.path, it.line, it.column, it.lineContent)
        }

        val message = if (declaration != null && location == null) {
            "${report.message}\n    (at ${DescriptorRenderer.COMPACT_WITH_SHORT_TYPES.render(declaration)})"
        } else {
            report.message
        }

        when (report.severity) {
            Severity.Error -> konanConfig.configuration.report(CompilerMessageSeverity.ERROR, message, location)
            Severity.Warning -> konanConfig.configuration.report(CompilerMessageSeverity.WARNING, message, location)
        }
    }
}
