package co.touchlab.skie.devsupport.irinspector

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.string.shouldContain
import java.io.ByteArrayOutputStream

class IrInspectorTest : BehaviorSpec({

    Given("File with code") {
        val file = tempfile(suffix = ".kt")
        file.writeText(
            """
            fun test() {
            }
        """.trimIndent()
        )

        When("I run the ir inspector") {
            val outputStream = ByteArrayOutputStream()

            runInspector(file.toPath(), outputStream)

            Then("I get the IR") {
                outputStream.toString() shouldContain "FUN name:test visibility:public modality:FINAL <> () returnType:kotlin.Unit"
            }
        }
    }
})
