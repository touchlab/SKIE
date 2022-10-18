import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.configurationcache.extensions.capitalized

val architecture = "uname -m".let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText).trim()

val architectureKotlinName = when (architecture) {
    "arm64" -> "macosArm64"
    "x86_64" -> "macosX64"
    else -> error("Unsupported architecture: $architecture")
}

val compileSwift = tasks.register<Exec>("compileSwift") {
    val frameworkDirectory = layout.projectDirectory.dir("../kotlin/build/bin/$architectureKotlinName/releaseFramework")
    val mainFile = layout.buildDirectory.file("main").get().asFile

    group = "build"
    dependsOn(":playground:kotlin:linkReleaseFramework${architectureKotlinName.capitalized()}")

    inputs.dir(frameworkDirectory)
    outputs.file(mainFile)

    doFirst {
        mkdir(layout.buildDirectory)
    }
    doFirst {
        println("---------------- Swift compilation ----------------")
    }
    commandLine(
        "arch",
        "-$architecture",
        "swiftc",
        "main.swift",
        "-F",
        frameworkDirectory.asFile.absolutePath,
        "-o",
        mainFile.absolutePath,
        // Workaround for https://github.com/apple/swift/issues/55127
        "-parse-as-library",
    )
}

val clean = tasks.register<Delete>("clean") {
    group = "build"
    delete("build")
    dependsOn(":playground:kotlin:clean")
}

tasks.register<Exec>("runSwift") {
    group = "build"
    dependsOn(clean)
    dependsOn(compileSwift).mustRunAfter(clean)
    doFirst {
        println("---------------- Program output ----------------")
    }
    commandLine("build/main")
}

tasks.register<SetupPlaygroundTask>("setupPlayground") {
    dependsOn(clean)

    testsDirectory = layout.projectDirectory.dir("../../acceptance-tests/src/test/resources").asFile
    kotlinPlaygroundDirectory = layout.projectDirectory.dir("../kotlin/src").asFile
    configDirectory = layout.projectDirectory.dir("../kotlin/swiftgen").asFile
    swiftPlaygroundFile = layout.projectDirectory.file("main.swift").asFile
}

abstract class SetupPlaygroundTask : DefaultTask() {

    @get:Input
    @set:Option(option = "acceptanceTest", description = "the test to be copied to the playground")
    var acceptanceTest: String? = null

    @InputDirectory
    lateinit var testsDirectory: File

    @OutputDirectory
    lateinit var kotlinPlaygroundDirectory: File

    @OutputDirectory
    lateinit var configDirectory: File

    @OutputFile
    lateinit var swiftPlaygroundFile: File

    @TaskAction
    fun execute() {
        checkInputs()

        val swiftTestFile = getSwiftTestFile()
        copySwiftTestFile(swiftTestFile)

        val kotlinPlaygroundSourcesDirectory = prepareKotlinPlaygroundSourcesDirectory()
        copyKotlinTestFiles(swiftTestFile, kotlinPlaygroundSourcesDirectory)

        prepareConfigDirectory()
        copyConfigFiles(swiftTestFile)
    }

    private fun checkInputs() {
        check(testsDirectory.exists()) { "Cannot find acceptance tests directory." }
        check(kotlinPlaygroundDirectory.exists()) { "Cannot find kotlin playground directory." }
        check(swiftPlaygroundFile.exists()) { "Cannot find swift playground file." }
    }

    private fun getSwiftTestFile(): File {
        requireNotNull(acceptanceTest) {
            "Missing property \"test\" that specifies which test should be copied to the playground. " +
                    "Pass the argument using \"-Ptest=...\""
        }

        val swiftTestFile = testsDirectory.resolve(acceptanceTest!! + ".swift")

        require(swiftTestFile.exists()) { "Test file \"$swiftTestFile\" does not exist." }
        require(swiftTestFile.startsWith(testsDirectory)) {
            "Test file \"$swiftTestFile\" must be located in the acceptance tests directory."
        }

        return swiftTestFile
    }

    private fun prepareKotlinPlaygroundSourcesDirectory(): File {
        val kotlinPlaygroundSourcesDirectory = kotlinPlaygroundDirectory.resolve("commonMain/kotlin")

        check(kotlinPlaygroundDirectory.exists()) { "Cannot find Kotlin playground module directory." }

        kotlinPlaygroundDirectory.deleteRecursively()
        kotlinPlaygroundSourcesDirectory.mkdirs()

        return kotlinPlaygroundSourcesDirectory
    }

    private fun copySwiftTestFile(swiftTestFile: File) {
        val swiftCode = swiftTestFile.readText().trim()

        val enhancedSwiftCode = enhanceSwiftCode(swiftCode)

        swiftPlaygroundFile.writeText(enhancedSwiftCode)
    }

    private fun enhanceSwiftCode(swiftCode: String): String =
        swiftCode
            .let(::addExitCallCheck)
            .let(::addMainFunction)
            .let(::addImports)

    private fun addExitCallCheck(code: String): String =
        "$code\n\nfatalError(\"Tested program ended without explicitly calling `exit(0)`.\")\n"

    private fun addMainFunction(code: String): String =
        "@main struct Main {\n\n    static func main() async {\n" +
                code.prependIndent("        ").trimEnd() +
                "\n    }\n}\n"

    private fun addImports(code: String): String =
        "import Foundation\nimport Kotlin\n\n$code"

    private fun copyKotlinTestFiles(swiftTestFile: File, kotlinPlaygroundSourcesDirectory: File) {
        swiftTestFile.walkUpToTestsDirectory {
            copySingleKotlinTestDirectory(it, kotlinPlaygroundSourcesDirectory)
        }
    }

    private fun copySingleKotlinTestDirectory(kotlinTestDirectory: File, kotlinPlaygroundSourcesDirectory: File) {
        val relocatedDirectory = prepareSingleKotlinPlaygroundDirectory(
            kotlinTestDirectory,
            kotlinPlaygroundSourcesDirectory,
        )

        kotlinTestDirectory.listFiles()
            ?.filter { it.extension == "kt" }
            ?.forEach { kotlinFile ->
                val playgroundKotlinFile = relocatedDirectory.resolve(kotlinFile.name)

                kotlinFile.copyTo(playgroundKotlinFile)
            }
    }

    private fun prepareSingleKotlinPlaygroundDirectory(
        kotlinTestDirectory: File,
        kotlinPlaygroundSourcesDirectory: File,
    ): File {
        val relocatedDirectory = kotlinPlaygroundSourcesDirectory.resolve(
            kotlinTestDirectory.absolutePath
                .removePrefix(testsDirectory.absolutePath)
                .removePrefix("/")
        )

        relocatedDirectory.mkdirs()

        return relocatedDirectory
    }

    private fun prepareConfigDirectory() {
        configDirectory.deleteRecursively()
        configDirectory.mkdirs()
    }

    private fun copyConfigFiles(swiftTestFile: File) {
        val configFiles = getConfigFiles(swiftTestFile)

        writeConfigFiles(configFiles)
    }

    private fun getConfigFiles(swiftTestFile: File): List<File> {
        val configFilesInReverseOrder = mutableListOf<File>()

        swiftTestFile.walkUpToTestsDirectory { directory ->
            val localConfigFiles = directory.listFiles()?.filter { it.name == "config.json" } ?: emptyList()

            configFilesInReverseOrder.addAll(localConfigFiles)
        }

        return configFilesInReverseOrder.reversed()
    }

    private fun writeConfigFiles(configFiles: List<File>) {
        configFiles.forEachIndexed { index, file ->
            val fileName = "config_${index + 1}.json"

            val fileContent = file.readText()

            configDirectory.resolve(fileName).writeText(fileContent)
        }
    }

    private fun File.walkUpToTestsDirectory(action: (File) -> Unit) {
        var currentDirectory = this.parentFile

        while (currentDirectory != testsDirectory) {
            action(currentDirectory)

            currentDirectory = currentDirectory.parentFile
        }
    }
}
