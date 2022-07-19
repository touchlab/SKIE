package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.KobjcTransform
import co.touchlab.swiftpack.spec.NameMangling.mangledClassName
import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spec.SwiftPackModule.Companion.write
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.SelfTypeName
import java.io.File

@Suppress("FunctionName")
class SwiftPackModuleBuilder(
    private val moduleName: String,
) {
    private val files = mutableListOf<FileSpec>()
    private val kobjcTransforms = mutableSetOf<KobjcTransform>()

    fun DeclaredTypeName.Companion.kotlin(qualifiedClassName: String): DeclaredTypeName {
        return qualifiedTypeName(
            "KotlinSwiftGen.${qualifiedClassName.mangledClassName}"
        )
    }

    fun PropertySpec.Companion.kotlin(propName: String): PropertySpec {
        return builder("KotlinSwiftGen_$propName", SelfTypeName.INSTANCE).build()
    }

    fun FunctionSpec.Companion.kotlin(funName: String): FunctionSpec {
        return builder("KotlinSwiftGen_$funName").build()
    }

    fun file(name: String, contents: FileSpec.Builder.() -> Unit): FileSpec {
        val builder = FileSpec.builder(name)
        builder.contents()
        val file = builder.build()
        files.add(file)
        return file
    }

    fun kobjcTransforms(block: KobjcTransformScope.() -> Unit) {
        val scope = KobjcTransformScope()
        scope.block()
    }

    fun build(): SwiftPackModule {
        return SwiftPackModule(
            moduleName,
            files.map {
                SwiftPackModule.TemplateFile(
                    name = it.name,
                    contents = it.toString()
                )
            },
            kobjcTransforms
        )
    }

    inner class KobjcTransformScope {
        fun hide(typeName: DeclaredTypeName) {
            kobjcTransforms.add(KobjcTransform.HideType(typeName.name))
        }

        fun rename(typeName: DeclaredTypeName, newName: String) {
            kobjcTransforms.add(KobjcTransform.RenameType(typeName.name, newName))
        }
    }

    object Config {
        var outputDir: File? = null
    }
}

fun buildSwiftPackModule(moduleName: String = "main", writeToOutputDir: Boolean = true, block: SwiftPackModuleBuilder.() -> Unit): SwiftPackModule {
    val context = SwiftPackModuleBuilder(moduleName)
    context.block()
    val template = context.build()
    if (writeToOutputDir) {
        val outputDir = checkNotNull(SwiftPackModuleBuilder.Config.outputDir) {
            "Output directory not configured! Either apply the SwiftPack Gradle plugin, set the SwiftTemplateBuilder.Config.outputDir, or pass false as the first parameter of buildSwiftTemplate."
        }
        outputDir.mkdirs()
        template.write(outputDir.resolve("$moduleName.swiftpack"))
    }
    return template
}
