package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.KotlinTypeReference
import co.touchlab.swiftpack.spec.function
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeSpec
import org.junit.jupiter.api.Test

class SwiftGenContextTest {

    @Test
    fun basicTest() {
        buildSwiftPackModule2 {
            val kotlinTest = KotlinTypeReference("co.touchlab.swiftkt.KotlinTest").applyTransform {
                hide()
            }
            val kotlinTestHelloWorld = kotlinTest.function("helloWorld")

            val swiftTest = file("SwiftTest") {
                val kotlinTestProperty = PropertySpec.varBuilder("kotlinTest", kotlinTest.swiftReference())
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

            // kobjcTransforms {
            //     type("") {
            //         hide()
            //
            //         property("") {
            //             hide()
            //         }
            //     }
            // }

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
