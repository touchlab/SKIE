package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptance_tests_framework.BuildConfig
import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.libraries.TestBuildConfig
import co.touchlab.skie.plugin.libraries.lockfile.DefaultLockfileUpdater
import co.touchlab.skie.plugin.libraries.lockfile.IncrementalLockfileUpdater
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import java.io.File
import kotlin.io.path.Path

class ExternalLibrariesTest : FunSpec(
    {
        System.setProperty("konan.home", BuildConfig.KONAN_HOME)

        val isSkieEnabled = "disableSkie" !in TestProperties
        val skieConfiguration = if (isSkieEnabled) {
            CompilerSkieConfigurationData(
                enabledConfigurationFlags = setOf(
                    SkieConfigurationFlag.Feature_CoroutinesInterop,
                    SkieConfigurationFlag.Feature_DefaultArgumentsInExternalLibraries,
                    SkieConfigurationFlag.Feature_FlowCombineConvertor,
                    SkieConfigurationFlag.Feature_FutureCombineExtension,
                    SkieConfigurationFlag.Feature_SwiftUIObserving,
                    SkieConfigurationFlag.Debug_VerifyDescriptorProviderConsistency,
                    SkieConfigurationFlag.Build_ConcurrentSkieCompilation,
                    SkieConfigurationFlag.Build_ParallelSkieCompilation,
                ),
                groups = listOf(
                    CompilerSkieConfigurationData.Group(
                        target = "",
                        overridesAnnotations = false,
                        items = mapOf(
                            "DefaultArgumentInterop.Enabled" to "true",
                            "ClassInterop.DeriveCInteropFrameworkNameFromCocoapods" to "false",
                            "TestConfigurationKeys.EnableVerifyFrameworkHeaderPhase" to "false",
                        ),
                    ),
                ),
            )
        } else {
            null
        }

        val lockfileUpdater = TestProperties["updateLockfile"]?.let {
            DefaultLockfileUpdater(Path(it))
        } ?: TestProperties["updateLockfileIncrementally"]?.let {
            IncrementalLockfileUpdater(Path(it))
        }

        check(!("updateLockfile" in TestProperties && "updateLockfileIncrementally" in TestProperties)) {
            "Cannot set both 'updateLockfile' and 'updateLockfileIncrementally' at the same time."
        }

        val activeFlagsDescription = listOf(
            "failedOnly",
            "libraryTest",
            "onlyIndices",
            "skipDependencyResolution",
            "skipKotlinCompilation",
            "skipSwiftCompilation",
            "disableSkie",
            "queryMavenCentral",
            "ignoreLockfile",
            "ignoreExpectedFailures",
            "ignoreDependencyConstraints",
            "updateLockfile",
            "updateLockfileIncrementally",
            "includeFailedTestsInLockfile",
            "convertLibraryDependenciesToTests",
            "skipTestsInLockfile",
            "keepTemporaryFiles",
            "onlyUnresolvedVersions",
        )
            .filter { it in TestProperties }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ", " [", "]") ?: ""

        val testSubdirectory = when {
            lockfileUpdater != null -> "update-lockfile"
            isSkieEnabled -> "skie-enabled"
            else -> "skie-disabled"
        }

        val testTmpDir = File(TestBuildConfig.BUILD).resolve(testSubdirectory).also {
            it.mkdirs()
        }

        val testDirectoryManager = TestDirectoryManager(testTmpDir)
        val testLoader = ExternalLibrariesTestLoader(testDirectoryManager, isSkieEnabled)
        val librariesToTest = testLoader.loadTests()

        val testRunner = ExternalLibrariesTestRunner(
            testFilter = TestFilter.active,
            skieConfigurationData = skieConfiguration,
            scopeSuffix = activeFlagsDescription,
            lockfileUpdater = lockfileUpdater,
        )

        testRunner.runTests(this, librariesToTest)
    },
) {

    override fun isolationMode(): IsolationMode = IsolationMode.SingleInstance
}
