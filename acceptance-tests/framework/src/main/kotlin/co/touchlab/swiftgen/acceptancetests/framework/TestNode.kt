package co.touchlab.swiftgen.acceptancetests.framework

import co.touchlab.swiftgen.acceptancetests.framework.internal.TestCodeParser
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readLines

sealed class TestNode {

    val name: String
        get() = path.name.removeSuffix(".swift")

    val fullName: String
        get() = listOfNotNull(parent?.fullName, name).joinToString(fullNameSeparator)

    abstract val directChildren: List<TestNode>

    protected abstract val path: Path

    protected abstract val parent: Container?

    companion object {

        private const val fullNameSeparator = "/"

        operator fun invoke(path: Path): TestNode =
            Container(path, null)

        operator fun invoke(path: Path, parent: Container): TestNode =
            if (path.isRegularFile()) Test(path, parent) else Container(path, parent)
    }

    data class Test constructor(
        public override val path: Path,
        override val parent: Container,
    ) : TestNode() {

        override val directChildren: List<TestNode> = emptyList()

        val kotlinFiles: List<Path>
            get() = parent.kotlinFiles

        val configFiles: List<Path>
            get() = parent.configFiles

        init {
            require(path.isRegularFile() && path.extension == "swift") { "Test $path is not a swift file." }
        }

        private val parsedTest = TestCodeParser.parse(path.readLines())

        val expectedResult: ExpectedTestResult = parsedTest.expectedResult

        val swiftCode: String = parsedTest.swiftCode

        override fun toString(): String = fullName

        fun testTempDirectory(tempFileSystemFactory: TempFileSystemFactory): Path =
            tempFileSystemFactory.tempDirectory.resolve(fullName)

        fun resultPath(tempFileSystemFactory: TempFileSystemFactory): Path =
            testTempDirectory(tempFileSystemFactory).resolve("result.txt")
    }

    data class Container constructor(
        override val path: Path,
        override val parent: Container?,
    ) : TestNode() {

        override val directChildren: List<TestNode> by lazy {
            path.listDirectoryEntries()
                .filter { it.isDirectChildren }
                .map { TestNode(it, this) }
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
