package co.touchlab.swiftpack.api

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeSpec
import org.junit.jupiter.api.Test

class SwiftGenContextTest {

    @Test
    fun basicTest() {
        buildSwiftPackModule {
            val kotlinTest = DeclaredTypeName.kotlin("co.touchlab.swiftkt.KotlinTest")
            val kotlinTestHelloWorld = FunctionSpec.kotlin("helloWorld")

            val swiftTest = file("SwiftTest") {
                val kotlinTestProperty = PropertySpec.varBuilder("kotlinTest", kotlinTest)
                    .build()

                addType(
                    TypeSpec.structBuilder("SwiftTest")
                        .addProperty(kotlinTestProperty)
                        .addFunction(
                            FunctionSpec.builder("helloWorld")
                                .addCode(
                                    """
                                        %[let kotlinClass: %T = %N%]
                                        %[kotlinClass.%N()%]${"\n"}
                                    """.trimIndent(),
                                    kotlinTest, kotlinTestProperty, kotlinTestHelloWorld,
                                )
                                .build()
                        )
                        .build()
                )
            }

            println(swiftTest.toString())
        }
        // FileMemberSpec.builder(
        //     TypeSpec.structBuilder(DeclaredTypeName)
        // )
        //
        // FileSpec.builder("SomeClass")
        //     .addMember()

    }

}
