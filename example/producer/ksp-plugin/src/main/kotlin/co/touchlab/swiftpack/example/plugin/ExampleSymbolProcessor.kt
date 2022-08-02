package co.touchlab.swiftpack.example.plugin

import co.touchlab.swiftpack.api.buildSwiftPackModule
import co.touchlab.swiftpack.api.kotlin
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.parameterizedBy

class ExampleSymbolProcessor: SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        buildSwiftPackModule {
            file("AnnotatedList") {
                addProperty(
                    PropertySpec.builder("annotatedKotlinInstances", DeclaredTypeName.typeName("Swift.Array").parameterizedBy(DeclaredTypeName.typeName("Swift.Any")), Modifier.PUBLIC)
                        .initializer(
                            CodeBlock.builder().apply {
                                add("[")
                                resolver.getSymbolsWithAnnotation("co.touchlab.swiftpack.example.ExampleAnnotation")
                                    .mapNotNull { it as? KSClassDeclaration }
                                    .forEach {
                                        add("%T(),", DeclaredTypeName.kotlin(it.qualifiedName!!.asString()))
                                    }
                                add("")
                                add("]")
                            }.build()
                        )
                        .build()
                )
            }

            kobjcTransforms {
                type("co.touchlab.swiftpack.example.SecondTest") {
                    hide()
                }

                type("co.touchlab.swiftpack.example.NotAnnotatedTest") {
                    rename("SwiftableName")
                }
            }
        }

        return emptyList()
    }
}

