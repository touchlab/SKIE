package co.touchlab.swiftgen.acceptancetests

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.acceptancetests.framework.AcceptanceTestsRunner
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.swiftgen.acceptancetests.framework.TestNode
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import kotlin.io.path.Path

class AcceptanceTests : FunSpec({
    val tempFileSystemFactory = TempFileSystemFactory(this)

    val runner = AcceptanceTestsRunner(tempFileSystemFactory, System.getenv("acceptanceTest"))

    val tests = TestNode(Path(BuildConfig.RESOURCES + "/tests"))

    runner.runTests(this, tests)
}) {

    override fun isolationMode(): IsolationMode = IsolationMode.SingleInstance
}