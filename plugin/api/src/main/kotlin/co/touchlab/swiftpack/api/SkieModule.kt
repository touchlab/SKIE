package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.module.ApiTransform
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType

interface SkieContext {
    val module: SkieModule
}

interface SkieModule {
    fun configure(configure: context(MutableSwiftContext) () -> Unit)

    fun file(name: String, contents: context(SwiftPoetContext) FileSpec.Builder.() -> Unit)
}

class DefaultSkieModule(): SkieModule {
    private val configureBlocks = mutableListOf<context(MutableSwiftContext) () -> Unit>()
    private val fileBlocks = mutableMapOf<String, MutableList<context(SwiftPoetContext) FileSpec.Builder.() -> Unit>>()
    private var configureBlocksConsumed = false

    override fun configure(configure: context(MutableSwiftContext) () -> Unit) {
        require(!configureBlocksConsumed) { "configure() must not be called again after consumed" }
        configureBlocks.add(configure)
    }

    override fun file(name: String, contents: context(SwiftPoetContext) FileSpec.Builder.() -> Unit) {
        fileBlocks.getOrPut(name) { mutableListOf() }.add(contents)
    }

    fun consumeConfigureBlocks(): List<context(MutableSwiftContext) () -> Unit> {
        configureBlocksConsumed = true
        val result = configureBlocks.toList()
        configureBlocks.clear()
        return result
    }

    fun produceFiles(context: SwiftPoetContext): List<FileSpec> {
        val result = mutableMapOf<String, FileSpec.Builder>()

        do {
            val consumedValues = fileBlocks.toMap()
            fileBlocks.clear()
            consumedValues.forEach { (fileName, blocks) ->
                blocks.forEach { block ->
                    with(context) {
                        block(result.getOrPut(fileName) { FileSpec.builder(fileName) })
                    }
                }
            }
        } while (fileBlocks.isNotEmpty())

        return result.values.map { it.build() }
    }
}

interface SwiftTypeName {
    val parent: SwiftTypeName?
    val separator: String
    val simpleName: String
    val originalSimpleName: String

    val originalQualifiedName: String
    val qualifiedName: String
}

class MutableSwiftTypeName(
    private val originalParent: MutableSwiftTypeName?,
    private val originalSeparator: String,
    override val originalSimpleName: String,
): SwiftTypeName {
    override var parent: MutableSwiftTypeName? = originalParent
    override var separator: String = originalSeparator
    override var simpleName = originalSimpleName

    val isChanged: Boolean
        get() = simpleName != originalSimpleName || separator != originalSeparator || parent != originalParent || parent?.isChanged == true

    // fun apply(rename: ApiTransform.TypeTransform.Rename) {
    //     val newSwiftName = when (val action = rename.action) {
    //         is ApiTransform.TypeTransform.Rename.Action.Prefix -> "${action.prefix}$originalSimpleName"
    //         is ApiTransform.TypeTransform.Rename.Action.Replace -> action.newName
    //         is ApiTransform.TypeTransform.Rename.Action.Suffix -> "$originalSimpleName${action.suffix}"
    //     }
    //
    //     if (rename.kind == ApiTransform.TypeTransform.Rename.Kind.ABSOLUTE) {
    //         parent = null
    //         separator = ""
    //     }
    // }

    override val originalQualifiedName: String
        get() {
            val parentName = parent?.originalQualifiedName ?: return originalSimpleName
            return "$parentName$separator$originalSimpleName"
        }

    override val qualifiedName: String
        get() {
            val parentName = parent?.qualifiedName ?: return simpleName
            return "$parentName$separator$simpleName"
        }
}

sealed interface SwiftBridgedName {
    data class Absolute(val name: String): SwiftBridgedName {
        override fun resolve(): String = name
    }
    data class Relative(val parent: SwiftTypeName, val childName: String): SwiftBridgedName {
        val typealiasName: String
            get() = "${parent.qualifiedName}__$childName"

        val typealiasValue: String
            get() = "${parent.qualifiedName}.$childName"

        override fun resolve(): String = typealiasName
    }

    fun resolve(): String

    companion object {
        operator fun invoke(parentOrNull: SwiftTypeName?, name: String): SwiftBridgedName {
            return if (parentOrNull == null) {
                Absolute(name)
            } else {
                Relative(parentOrNull, name)
            }
        }
    }
}


interface SwiftClassContext {
    val ClassDescriptor.swiftName: SwiftTypeName

    val ClassDescriptor.isHiddenFromSwift: Boolean

    val ClassDescriptor.isRemovedFromSwift: Boolean

    val ClassDescriptor.swiftBridgeType: SwiftBridgedName?
}

interface MutableSwiftClassContext: SwiftClassContext {
    override var ClassDescriptor.swiftName: MutableSwiftTypeName

    override var ClassDescriptor.isHiddenFromSwift: Boolean

    override var ClassDescriptor.isRemovedFromSwift: Boolean

    override var ClassDescriptor.swiftBridgeType: SwiftBridgedName?
}

interface SwiftTypeContext {
    val KotlinType.swiftName: String
}

object SkieContextKey: CompilerConfigurationKey<SkieContext>("SkieContext")

val CommonBackendContext.skieContext: SkieContext
    get() = configuration.getNotNull(SkieContextKey)

interface SwiftPropertyContext {
    val PropertyDescriptor.originalSwiftName: String

    val PropertyDescriptor.swiftName: String

    val PropertyDescriptor.isHiddenFromSwift: Boolean

    val PropertyDescriptor.isRemovedFromSwift: Boolean
}

interface MutableSwiftPropertyContext: SwiftPropertyContext {
    override var PropertyDescriptor.swiftName: String

    override var PropertyDescriptor.isHiddenFromSwift: Boolean

    override var PropertyDescriptor.isRemovedFromSwift: Boolean
}

interface SwiftFunctionContext {
    val FunctionDescriptor.originalSwiftName: String

    val FunctionDescriptor.swiftName: String

    val FunctionDescriptor.isHiddenFromSwift: Boolean

    val FunctionDescriptor.isRemovedFromSwift: Boolean
}

interface MutableSwiftFunctionContext: SwiftFunctionContext {
    override var FunctionDescriptor.swiftName: String

    override var FunctionDescriptor.isHiddenFromSwift: Boolean

    override var FunctionDescriptor.isRemovedFromSwift: Boolean
}

interface SwiftContext: SwiftClassContext, SwiftTypeContext, SwiftPropertyContext, SwiftFunctionContext

interface SwiftPoetContext: SwiftContext {
    val KotlinType.spec: TypeName

    val ClassDescriptor.spec: DeclaredTypeName

    val PropertyDescriptor.spec: PropertySpec

    val FunctionDescriptor.spec: FunctionSpec
}

interface MutableSwiftContext: MutableSwiftClassContext, MutableSwiftPropertyContext, MutableSwiftFunctionContext {

}

// class DefaultSwiftContext(
//     private val namer: ObjCExportNamer,
// ): SwiftContext {
//
//     override val PropertyDescriptor.originalSwiftName: String
//         get() = namer.getPropertyName(this)
//
//     override var PropertyDescriptor.swiftName: String
//         get() = TODO("transforms.get(this)?.newName ?: originalSwiftName")
//         set(value) { TODO("transforms.get(this).newName = value") }
//
//     override var PropertyDescriptor.isHiddenFromSwift: Boolean
//         get() = TODO()
//         set(value) {}
//
//     override var PropertyDescriptor.isRemovedFromSwift: Boolean
//         get() = TODO("Not yet implemented")
//         set(value) {}
//
//
// }
