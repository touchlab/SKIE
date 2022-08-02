package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.KobjcTransforms
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
    private val kobjcTransformsScope = KobjcTransformScope()

    val files: Set<FileSpec> get() = mutableFiles

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
        kobjcTransformsScope.block()
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
            kobjcTransformsScope.build(),
        )
    }

    @DslMarker
    annotation class KobjcScopeMarker

    @KobjcScopeMarker
    class KobjcTransformScope(
        private val types: MutableMap<String, TypeTransformScope> = mutableMapOf(),
        private val properties: MutableMap<String, PropertyTransformScope> = mutableMapOf(),
        private val functions: MutableMap<String, FunctionTransformScope> = mutableMapOf(),
    ) {
        fun type(name: String, builder: TypeTransformScope.() -> Unit) {
            val scope = types.getOrPut(name) { TypeTransformScope(name) }
            scope.builder()
        }

        fun property(name: String, builder: PropertyTransformScope.() -> Unit) {
            val scope = properties.getOrPut(name) { PropertyTransformScope(name) }
            scope.builder()
        }

        fun function(name: String, builder: FunctionTransformScope.() -> Unit) {
            val scope = functions.getOrPut(name) { FunctionTransformScope(name) }
            scope.builder()
        }

        internal fun build(): KobjcTransforms {
            return KobjcTransforms(
                types = types.mapValues { it.value.build() },
                properties = properties.mapValues { it.value.build() },
                functions = functions.mapValues { it.value.build() },
            )
        }

        @KobjcScopeMarker
        class TypeTransformScope(
            private val name: String,
            private var hide: Boolean = false,
            private var remove: Boolean = false,
            private var rename: String? = null,
            private var bridge: String? = null,
            private val properties: MutableMap<String, PropertyTransformScope> = mutableMapOf(),
            private val functions: MutableMap<String, FunctionTransformScope> = mutableMapOf(),
        ) {
            fun remove() {
                remove = true
            }

            fun hide() {
                hide = true
            }

            fun rename(newSwiftName: String) {
                rename = newSwiftName
            }

            fun bridge(swiftType: String) {
                bridge = swiftType
            }

            fun property(name: String, builder: PropertyTransformScope.() -> Unit) {
                val scope = properties.getOrPut(name) { PropertyTransformScope(name) }
                scope.builder()
            }

            fun method(name: String, builder: FunctionTransformScope.() -> Unit) {
                val scope = functions.getOrPut(name) { FunctionTransformScope(name) }
                scope.builder()
            }

            internal fun build(): KobjcTransforms.TypeTransform {
                return KobjcTransforms.TypeTransform(
                    type = name,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                    bridge = bridge,
                    properties = properties.mapValues { it.value.build() },
                    methods = functions.mapValues { it.value.build() },
                )
            }
        }

        @KobjcScopeMarker
        class PropertyTransformScope(
            private val name: String,
            private var hide: Boolean = false,
            private var remove: Boolean = false,
            private var rename: String? = null,
        ) {
            fun remove() {
                remove = true
            }

            fun hide() {
                hide = true
            }

            fun rename(newSwiftName: String) {
                rename = newSwiftName
            }

            internal fun build(): KobjcTransforms.PropertyTransform {
                return KobjcTransforms.PropertyTransform(
                    name = name,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                )
            }
        }

        @KobjcScopeMarker
        class FunctionTransformScope(
            private val name: String,
            private var hide: Boolean = false,
            private var remove: Boolean = false,
            private var rename: String? = null,
        ) {
            fun remove() {
                remove = true
            }

            fun hide() {
                hide = true
            }

            fun rename(newSwiftName: String) {
                rename = newSwiftName
            }

            internal fun build(): KobjcTransforms.FunctionTransform {
                return KobjcTransforms.FunctionTransform(
                    name = name,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                )
            }
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
