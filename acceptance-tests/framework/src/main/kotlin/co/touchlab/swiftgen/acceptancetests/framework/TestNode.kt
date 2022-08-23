package co.touchlab.swiftgen.acceptancetests.framework

import co.touchlab.swiftgen.acceptancetests.framework.internal.TestCodeParser
import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import java.nio.file.Path
import kotlin.io.path.*

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

        init {
            require(path.isRegularFile() && path.extension == "swift") { "Test $path is not a swift file." }
        }

        private val parsedTest = TestCodeParser.parse(path.readLines())

        val expectedResult: ExpectedTestResult = parsedTest.expectedResult

        val configuration: SwiftGenConfiguration = parsedTest.configuration

        val configurationChanges: Map<String, String> = parsedTest.configurationChanges

        val swiftCode: String = parsedTest.swiftCode

        override fun toString(): String = fullName
    }

    data class Container constructor(
        override val path: Path,
        override val parent: Container?,
    ) : TestNode() {

        override val directChildren: List<TestNode> by lazy {
            path.listDirectoryEntries()
                .filterNot { it.isKotlinFile }
                .map { TestNode(it, this) }
        }

        val kotlinFiles: List<Path> by lazy {
            path.listDirectoryEntries()
                .filter { it.isKotlinFile } +
                    (parent?.kotlinFiles ?: emptyList())
        }

        private val Path.isKotlinFile: Boolean
            get() = extension == "kt"

        init {
            require(path.isDirectory()) { "Container $path is not a directory." }
        }

        override fun toString(): String = fullName
    }
}
