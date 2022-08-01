package co.touchlab.swiftgen.acceptancetests.framework

import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.TestRunner
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk

class TestRunnerTest : ShouldSpec({

    val helloWorldKotlin = """
            object KotlinKt {
                fun printHelloWorld() {
                    println("Hello world")
                }
            }
        """.trimIndent()

    val helloWorldOutput = """
            ---------------- Program output ----------------
            Hello world
            
            
        """.trimIndent()

    test(
        name = "success",
        kotlin = helloWorldKotlin,
        swift = """
            import Foundation
            import Kotlin
            
            KotlinKt.shared.printHelloWorld()
            
            exit(0)
        """.trimIndent(),
        expectedResult = TestResult.Success(helloWorldOutput)
    )

    test(
        name = "missing exit",
        kotlin = helloWorldKotlin,
        swift = """
            import Foundation
            import Kotlin

            KotlinKt.shared.printHelloWorld()
        """.trimIndent(),
        expectedResult = TestResult.MissingExit(helloWorldOutput)
    )

    test(
        name = "Incorrect output",
        kotlin = helloWorldKotlin,
        swift = """
            import Foundation
            import Kotlin

            KotlinKt.shared.printHelloWorld()

            exit(1)
        """.trimIndent(),
        expectedResult = TestResult.IncorrectOutput(helloWorldOutput, 1)
    )

    test(
        name = "runtime error",
        kotlin = helloWorldKotlin,
        swift = """
            import Foundation
            import Kotlin

            KotlinKt.shared.printHelloWorld()

            func divide(lhs: Int, rhs: Int) -> Int {
                return lhs / rhs
            }

            divide(lhs: 1, rhs: 0)
        """.trimIndent(),
        expectedResult = TestResult.RuntimeError(
            helloWorldOutput, error = """
            Fatal error: Division by zero
        """.trimIndent()
        )
    )

    test(
        name = "Swift compilation error",
        kotlin = helloWorldKotlin,
        swift = """
            KotlinKt.shared.printHelloWorld()
        """.trimIndent(),
        expectedResult = TestResult.SwiftCompilationError(
            """
        """.trimIndent(), error = """
            error: cannot find 'KotlinKt'
        """.trimIndent()
        )
    )

    test(
        name = "Kotlin compilation error",
        kotlin = """
            fun printHelloWorld() {
                x()
            }
        """.trimIndent(),
        swift = "",
        expectedResult = TestResult.KotlinCompilationError(
            """
        """.trimIndent(), error = """
            Unresolved reference: x
        """.trimIndent()
        )
    )
})

private fun ShouldSpec.test(
    name: String,
    kotlin: String,
    swift: String,
    expectedResult: TestResult,
) {
    val tempFileSystem = TempFileSystem(this)

    should(name) {
        val kotlinFile = tempfile(suffix = ".kt")
        kotlinFile.writeText(kotlin)

        val swiftFile = tempfile(suffix = ".swift")
        swiftFile.writeText(swift)

        val test = mockk<TestNode.Test>()
        every { test.kotlinFiles } returns listOf(kotlinFile.toPath())
        every { test.swiftFile } returns swiftFile.toPath()

        val result = TestRunner(tempFileSystem).runTest(test)

        if (result.javaClass != expectedResult.javaClass) {
            result shouldBe expectedResult
        }

        result.logs shouldBe expectedResult.logs
        when (result) {
            is TestResult.RuntimeError -> result.error shouldContain (expectedResult as TestResult.RuntimeError).error
            is TestResult.SwiftCompilationError -> {
                result.error shouldContain (expectedResult as TestResult.SwiftCompilationError).error
            }
            is TestResult.KotlinCompilationError -> {
                result.error shouldContain (expectedResult as TestResult.KotlinCompilationError).error
            }
            else -> {}
        }
    }
}
