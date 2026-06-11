package co.touchlab.skie.acceptancetests.framework.internal.skie

import co.touchlab.skie.phases.KirPhase
import co.touchlab.skie.phases.descriptorProvider

/**
 * Regression guard for the Kotlin 2.4.0 external-dependency reporting fix.
 *
 * Kotlin 2.4.0 removed `UserVisibleIrModulesSupport`, and SKIE now reconstructs `descriptorProvider.externalDependencies`
 * by deserializing the `-Xexternal-dependencies` file the Kotlin Gradle plugin forwards to the native link. None of the
 * existing test harnesses pass that file, so this behavior was previously untested (the broken `emptySet()` stub passed
 * the whole suite).
 *
 * When a compiled test module contains a class named [MARKER_CLASS_NAME], this phase asserts that the external
 * dependencies were actually reported (the set is non-empty and contains [EXPECTED_MODULE]). With the broken stub the
 * set is empty, so the [check] fails and crashes the compilation, turning the regression red. For every other test the
 * marker class is absent and this phase is a no-op.
 */
object VerifyExternalDependenciesAreReportedPhase : KirPhase {

    context(KirPhase.Context)
    override suspend fun execute() {
        val hasMarker = descriptorProvider.exposedClasses.any { it.name.asString() == MARKER_CLASS_NAME }
        if (!hasMarker) {
            return
        }

        val reported = descriptorProvider.externalDependencies

        check(reported.isNotEmpty()) {
            "External dependencies were not reported. `getExternalDependencies` should read the -Xexternal-dependencies " +
                "file, but `descriptorProvider.externalDependencies` was empty."
        }

        val reportedNames = reported.flatMap { it.id.uniqueNames }
        check(EXPECTED_MODULE in reportedNames) {
            "Expected external dependency '$EXPECTED_MODULE' to be reported, but got: $reportedNames"
        }
    }

    const val MARKER_CLASS_NAME: String = "VerifyExternalDependenciesAreReported"

    const val EXPECTED_MODULE: String = "com.example:foo"
}
