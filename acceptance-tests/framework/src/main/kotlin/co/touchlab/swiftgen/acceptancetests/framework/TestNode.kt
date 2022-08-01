package co.touchlab.swiftgen.acceptancetests.framework

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

        val swiftFile: Path
            get() = path

        init {
            require(path.isRegularFile() && path.endsWith(".swift")) { "Test $path is not a swift file." }
        }
    }

    data class Container constructor(
        override val path: Path,
        override val parent: Container?,
    ) : TestNode() {

        override val directChildren: List<TestNode> =
            path.listDirectoryEntries()
                .filterNot { it.isKotlinFile }
                .map { TestNode(it) }

        val kotlinFiles: List<Path> =
            path.listDirectoryEntries()
                .filter { it.isKotlinFile } +
                    (parent?.kotlinFiles ?: emptyList())

        private val Path.isKotlinFile: Boolean
            get() = name.endsWith(".kt")

        init {
            require(path.isDirectory()) { "Container $path is not a directory." }
        }
    }
}
