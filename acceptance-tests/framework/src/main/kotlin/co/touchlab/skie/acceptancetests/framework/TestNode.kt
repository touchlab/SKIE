package co.touchlab.skie.acceptancetests.framework

import co.touchlab.skie.acceptancetests.framework.internal.TestCodeParser
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readLines

sealed class TestNode {

    val name: String
        get() = testName(path)

    val fullName: String
        get() = listOfNotNull(parent?.fullName, name).joinToString(fullNameSeparator)

    abstract val directChildren: List<TestNode>

    abstract val outputPath: Path

    protected abstract val path: Path

    protected abstract val parent: Container?

    companion object {

        private const val fullNameSeparator = "/"

        operator fun invoke(path: Path, outputPath: Path, compilerConfiguration: CompilerConfiguration): TestNode =
            Container(path, outputPath, null, compilerConfiguration)

        operator fun invoke(path: Path, outputPath: Path, parent: Container, compilerConfiguration: CompilerConfiguration): TestNode =
            if (path.isRegularFile()) {
                Test(path, outputPath, parent, compilerConfiguration)
            } else {
                Container(path, outputPath, parent, compilerConfiguration)
            }

        fun testName(path: Path): String =
            path.name.removeSuffix(".swift")
    }

    data class Test constructor(
        public override val path: Path,
        override val outputPath: Path,
        override val parent: Container,
        val compilerConfiguration: CompilerConfiguration,
    ) : TestNode() {

        override val directChildren: List<TestNode> = emptyList()

        val kotlinFiles: List<Path>
            get() = parent.kotlinFiles

        val configFiles: List<Path>
            get() = parent.configFiles

        val resultPath: Path
            get() = outputPath.resolve("result.txt")

        private val fileLines = path.readLines()

        val isActive: Boolean
            get() = fileLines.firstOrNull()?.let { !skippedTestRegex.containsMatchIn(it) } ?: true

        init {
            require(path.isRegularFile() && path.extension == "swift") { "Test $path is not a swift file." }
        }

        private val parsedTest by lazy {
            TestCodeParser.parse(fileLines)
        }

        val expectedResult: ExpectedTestResult
            get() = parsedTest.expectedResult

        val swiftCode: String
            get() = parsedTest.swiftCode

        override fun toString(): String = fullName

        private companion object {

            private val skippedTestRegex = Regex("^#\\s*Skip", RegexOption.IGNORE_CASE)
        }
    }

    data class Container constructor(
        override val path: Path,
        override val outputPath: Path,
        override val parent: Container?,
        private val compilerConfiguration: CompilerConfiguration,
    ) : TestNode() {

        override val directChildren: List<TestNode> by lazy {
            path.listDirectoryEntries()
                .filter { it.isDirectChildren }
                .map { TestNode(it, outputPath.resolve(testName(it)), this, compilerConfiguration) }
        }

        val kotlinFiles: List<Path> by lazy {
            path.listDirectoryEntries()
                .filter { it.isKotlinFile } +
                (parent?.kotlinFiles ?: emptyList())
        }

        val configFiles: List<Path> by lazy {
            (parent?.configFiles ?: emptyList()) +
                path.listDirectoryEntries()
                    .filter { it.isConfigFile }
        }

        private val Path.isKotlinFile: Boolean
            get() = this.extension == "kt"

        private val Path.isConfigFile: Boolean
            get() = this.name == "config.json"

        private val Path.isDirectChildren: Boolean
            get() = this.isDirectory() || this.extension == "swift"

        init {
            require(path.isDirectory()) { "Container $path is not a directory." }
        }

        override fun toString(): String = fullName
    }
}
