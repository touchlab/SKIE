package co.touchlab.swiftgen.sealed.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.sealed.acceptancetests.framework.internal.TestResult

internal sealed interface IntermediateResult<out T> {

    fun <R> map(action: (T) -> R): IntermediateResult<R>

    fun <R> flatMap(action: (T) -> IntermediateResult<R>): IntermediateResult<R>

    fun finalize(action: (T) -> TestResult): TestResult

    data class Value<T>(val value: T) : IntermediateResult<T> {

        override fun <R> map(action: (T) -> R): IntermediateResult<R> =
            Value(action(value))

        override fun <R> flatMap(action: (T) -> IntermediateResult<R>): IntermediateResult<R> =
            action(value)

        override fun finalize(action: (T) -> TestResult): TestResult =
            action(value)
    }

    data class Error(val testResult: TestResult) : IntermediateResult<Nothing> {

        override fun <R> map(action: (Nothing) -> R): IntermediateResult<R> =
            this

        override fun <R> flatMap(action: (Nothing) -> IntermediateResult<R>): IntermediateResult<R> =
            this

        override fun finalize(action: (Nothing) -> TestResult): TestResult =
            testResult
    }
}