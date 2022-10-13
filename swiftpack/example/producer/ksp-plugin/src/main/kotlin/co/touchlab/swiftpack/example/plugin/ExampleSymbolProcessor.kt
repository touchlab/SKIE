package co.touchlab.swiftpack.example.plugin

import co.touchlab.swiftpack.api.buildSwiftPackModule
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

class ExampleSymbolProcessor: SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        buildSwiftPackModule {
            // file("AnnotatedList") {
            //     addProperty(
            //         PropertySpec.builder("annotatedKotlinInstances", DeclaredTypeName.typeName("Swift.Array").parameterizedBy(DeclaredTypeName.typeName("Swift.Any")), Modifier.PUBLIC)
            //             .initializer(
            //                 CodeBlock.builder().apply {
            //                     add("[")
            //                     resolver.getSymbolsWithAnnotation("co.touchlab.swiftpack.example.ExampleAnnotation")
            //                         .mapNotNull { it as? KSClassDeclaration }
            //                         .forEach {
            //                             add("%T(),", KotlinClass(it.qualifiedName!!.asString()).tem())
            //                         }
            //                     add("")
            //                     add("]")
            //                 }.build()
            //             )
            //             .build()
            //     )
            // }
            //
            // kobjcTransforms {
            //     type("co.touchlab.swiftpack.example.SecondTest") {
            //         hide()
            //     }
            //
            //     type("co.touchlab.swiftpack.example.NotAnnotatedTest") {
            //         rename("SwiftableName")
            //     }
            // }
        }

        return emptyList()
    }
}
