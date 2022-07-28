package co.touchlab.swiftgen.sealed.acceptancetests

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.sealed.acceptancetests.framework.AcceptanceTestsRunner
import co.touchlab.swiftgen.sealed.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.sealed.acceptancetests.framework.TestNode
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import kotlin.io.path.Path

class AcceptanceTests : FunSpec({
    val tempFileSystem = TempFileSystem(this)

    context("acceptance tests") {
        val runner = AcceptanceTestsRunner(tempFileSystem, System.getProperty("acceptanceTest"))

        val tests = TestNode(Path(BuildConfig.RESOURCES + "/tests"))

        runner.runTests(this, tests)
    }
}) {

    override fun isolationMode(): IsolationMode = IsolationMode.SingleInstance
}