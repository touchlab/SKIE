package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.util.Reporter

class FixTypeConflictWithFrameworkNamePhase(
    private val skieModule: SkieModule,
    private val framework: FrameworkLayout,
    private val reporter: Reporter,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.Last) {
            val frameworkName = SwiftFqName.Local.TopLevel(framework.moduleName)

            renameClasses(frameworkName)

            renameFiles(frameworkName)
        }
    }

    private fun MutableSwiftModelScope.renameClasses(frameworkName: SwiftFqName.Local.TopLevel) {
        exposedClasses
            .filter { it.swiftIrDeclaration.publicName == frameworkName }
            .forEach {
                it.identifier += "_"

                reporter.report(
                    Reporter.Severity.Warning,
                    "Declaration '${it.classDescriptor.name.asString()}' was renamed to '${it.identifier}' " +
                        "because it has the same name as the produced framework which is forbidden.",
                    it.classDescriptor,
                )
            }
    }

    private fun MutableSwiftModelScope.renameFiles(frameworkName: SwiftFqName.Local.TopLevel) {
        exposedFiles
            .filter { it.swiftIrDeclaration.publicName == frameworkName }
            .forEach {
                reporter.report(
                    Reporter.Severity.Warning,
                    "File class '${it.identifier}' was renamed to '${it.identifier}_' " +
                        "because it has the same name as the produced framework which is forbidden.",
                )

                it.identifier += "_"
            }
    }
}
