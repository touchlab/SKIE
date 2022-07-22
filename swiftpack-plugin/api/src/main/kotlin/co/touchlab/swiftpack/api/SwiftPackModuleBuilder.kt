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
    private val mutableFiles = mutableSetOf<FileSpec>()
    private val mutableKobjcTransforms = mutableSetOf<KobjcTransform>()

    val files: Set<FileSpec> get() = mutableFiles
    val kobjcTransforms: Set<KobjcTransform> get() = mutableKobjcTransforms

    fun file(name: String, contents: FileSpec.Builder.() -> Unit): FileSpec {
        val builder = FileSpec.builder(name)
        builder.contents()
        val file = builder.build()
        mutableFiles.add(file)
        return file
    }

    fun addFile(file: FileSpec): SwiftPackModuleBuilder {
        mutableFiles.add(file)
        return this
    }

    fun kobjcTransforms(block: KobjcTransformScope.() -> Unit) {
        val scope = KobjcTransformScope()
        scope.block()
    }

    fun build(): SwiftPackModule {
        return SwiftPackModule(
            moduleName,
            mutableFiles.map {
                SwiftPackModule.TemplateFile(
                    name = it.name,
                    contents = it.toString()
                )
            }.sortedBy { it.name },
            mutableKobjcTransforms
        )
    }

    inner class KobjcTransformScope {
        fun hide(typeName: DeclaredTypeName) {
            mutableKobjcTransforms.add(KobjcTransform.HideType(typeName.name))
        }

        fun rename(typeName: DeclaredTypeName, newName: String) {
            mutableKobjcTransforms.add(KobjcTransform.RenameType(typeName.name, newName))
        }
    }

    object Config {
        var outputDir: File? = null
    }
}

val SWIFTPACK_KOTLIN_TYPE_PREFIX = "KotlinSwiftGen"

fun DeclaredTypeName.Companion.kotlin(qualifiedClassName: String): DeclaredTypeName {
    return qualifiedTypeName(
        "$SWIFTPACK_KOTLIN_TYPE_PREFIX.${qualifiedClassName.mangledClassName}"
    )
}

fun PropertySpec.Companion.kotlin(propName: String): PropertySpec {
    return builder("${SWIFTPACK_KOTLIN_TYPE_PREFIX}_$propName", SelfTypeName.INSTANCE).build()
}

fun FunctionSpec.Companion.kotlin(funName: String): FunctionSpec {
    return builder("${SWIFTPACK_KOTLIN_TYPE_PREFIX}_$funName").build()
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
