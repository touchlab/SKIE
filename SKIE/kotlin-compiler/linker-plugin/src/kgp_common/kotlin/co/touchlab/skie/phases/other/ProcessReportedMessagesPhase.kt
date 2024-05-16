package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase
import co.touchlab.skie.phases.descriptorKirProvider
import co.touchlab.skie.phases.descriptorReporter
import co.touchlab.skie.phases.konanConfig
import co.touchlab.skie.util.Reporter
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.descriptorUtil.module

object ProcessReportedMessagesPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        kirReporter.reportAll(descriptorKirProvider::findDeclarationDescriptor)

        descriptorReporter.reportAll { it }
    }

    context(LinkPhase.Context)
    private fun <T> Reporter<T>.reportAll(findDeclarationDescriptor: (T) -> DeclarationDescriptor?) {
        this.reports.forEach {
            report(it, findDeclarationDescriptor)
        }
    }

    context(LinkPhase.Context)
    private fun <T> report(report: Reporter.Report<T>, findDeclarationDescriptor: (T) -> DeclarationDescriptor?) {
        val declarationDescriptor = report.source?.let { findDeclarationDescriptor(it) }

        report(report, declarationDescriptor)
    }

    context(LinkPhase.Context)
    private fun <T> report(report: Reporter.Report<T>, declaration: DeclarationDescriptor?) {
        val location = MessageUtil.psiElementToMessageLocation(declaration?.findPsi())?.let {
            CompilerMessageLocation.create(it.path, it.line, it.column, it.lineContent)
        }

        val message = if (declaration != null && location == null) {
            "${report.message}\n    (at ${DescriptorRenderer.DEBUG_TEXT.render(declaration)} from module ${declaration.module.name})"
        } else {
            report.message
        }

        when (report.severity) {
            Reporter.Severity.Error -> konanConfig.configuration.report(CompilerMessageSeverity.ERROR, message, location)
            Reporter.Severity.Warning -> konanConfig.configuration.report(CompilerMessageSeverity.WARNING, message, location)
        }
    }
}
