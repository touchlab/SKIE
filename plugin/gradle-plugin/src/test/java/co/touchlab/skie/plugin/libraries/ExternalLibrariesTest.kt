package co.touchlab.skie.plugin.libraries

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import java.io.File

class ExternalLibrariesTest: FunSpec({
    val testTmpDir = File(System.getProperty("testTmpDir"))

    val preWarmer = ExternalLibrariesTestPreWarmer(testTmpDir)
    val testKitDir = preWarmer.warmUp()

    val onlyIndices = setOf<Int>(
        // 0, 1, 2, 3, 4
    )

    val testLoader = ExternalLibrariesTestLoader(testTmpDir)
    val librariesToTest = testLoader.loadLibrariesToTest(onlyIndices)

    val dirPreparer = ExternalLibrariesTestDirPreparer(testTmpDir)
    val rootDir = dirPreparer.prepareRootDir(librariesToTest)
    dirPreparer.prepareLibraryDirs(librariesToTest)

    val testRunner = ExternalLibrariesTestRunner(testTmpDir = testTmpDir, rootDir = rootDir, testKitDir = testKitDir)
    testRunner.runTests(this, librariesToTest)
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.SingleInstance
}
