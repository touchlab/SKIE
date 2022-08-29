package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.KobjcTransforms
import co.touchlab.swiftpack.spec.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.KotlinFileReference
import co.touchlab.swiftpack.spec.KotlinFunctionReference
import co.touchlab.swiftpack.spec.KotlinPackageReference
import co.touchlab.swiftpack.spec.KotlinPropertyReference
import co.touchlab.swiftpack.spec.KotlinTypeReference
import co.touchlab.swiftpack.spec.MemberParentReference
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_ENUM_ENTRY_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_FUNCTION_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_PROPERTY_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_TYPE_PREFIX
import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spec.SwiftPackModule.Companion.write
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.SelfTypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.isFileClass
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.utils.addToStdlib.getOrPut
import java.io.File
import java.util.concurrent.atomic.AtomicInteger


class SwiftPackModuleBuilder(
    private val moduleName: String,
) {
    private val mutableFiles = mutableSetOf<FileSpec>()
    private val kobjcTransformsScope = KobjcTransformScope()

    val files: Set<FileSpec> get() = mutableFiles

    private val referenceCounter = AtomicInteger(-1)
    private val typeReferences = ReferenceMap<KotlinTypeReference>(SWIFTPACK_KOTLIN_TYPE_PREFIX, referenceCounter)
    private val propertyReferences = ReferenceMap<KotlinPropertyReference>(SWIFTPACK_KOTLIN_PROPERTY_PREFIX, referenceCounter)
    private val functionReferences = ReferenceMap<KotlinFunctionReference>(SWIFTPACK_KOTLIN_FUNCTION_PREFIX, referenceCounter)
    private val enumEntryReferences = ReferenceMap<KotlinEnumEntryReference>(SWIFTPACK_KOTLIN_ENUM_ENTRY_PREFIX, referenceCounter)

    fun KotlinTypeReference.swiftReference(): DeclaredTypeName {
        val ref = typeReferences.getReference(this)
        return DeclaredTypeName.typeName(".$ref")
    }

    fun KotlinTypeReference.applyTransform(transform: KobjcTransformScope.TypeTransformScope.() -> Unit): KotlinTypeReference {
        kobjcTransformsScope.type(this, transform)
        return this
    }

    fun KotlinPropertyReference.swiftReference(): PropertySpec {
        val ref = propertyReferences.getReference(this)
        return PropertySpec.builder(ref, SelfTypeName.INSTANCE).build()
    }

    fun KotlinPropertyReference.applyTransform(transform: KobjcTransformScope.PropertyTransformScope.() -> Unit): KotlinPropertyReference {
        kobjcTransformsScope.property(this, transform)
        return this
    }

    fun KotlinFunctionReference.swiftReference(): FunctionSpec {
        val ref = functionReferences.getReference(this)
        return FunctionSpec.builder(ref).build()
    }

    fun KotlinFunctionReference.applyTransform(transform: KobjcTransformScope.FunctionTransformScope.() -> Unit): KotlinFunctionReference {
        kobjcTransformsScope.function(this, transform)
        return this
    }

    fun KotlinEnumEntryReference.swiftReference(): PropertySpec {
        val ref = enumEntryReferences.getReference(this)
        return PropertySpec.builder(ref, SelfTypeName.INSTANCE).build()
    }

    fun KotlinEnumEntryReference.applyTransform(transform: KobjcTransformScope.EnumEntryTransformScope.() -> Unit): KotlinEnumEntryReference {
        kobjcTransformsScope.enumEntry(this, transform)
        return this
    }

    fun ClassDescriptor.reference(): KotlinTypeReference {
        return KotlinTypeReference(containingDeclaration.reference(), name.asString())
    }

    fun IrClass.reference(): KotlinTypeReference {
        return KotlinTypeReference(parent.reference(), name.asString())
    }

    fun PropertyDescriptor.reference(): KotlinPropertyReference {
        return KotlinPropertyReference(containingDeclaration.reference(), name.asString())
    }

    fun IrProperty.reference(): KotlinPropertyReference {
        return KotlinPropertyReference(parent.reference(), name.asString())
    }

    fun FunctionDescriptor.reference(): KotlinFunctionReference {
        return KotlinFunctionReference(containingDeclaration.reference(), name.asString(), valueParameters.map { param ->
            requireNotNull(param.type.constructor.declarationDescriptor?.reference()) {
                "Function reference parameter type has no declaration descriptor"
            }
        })
    }

    fun IrFunction.reference(): KotlinFunctionReference {
        return KotlinFunctionReference(parent.reference(), name.asString(), valueParameters.map { param ->
            requireNotNull(param.type.originalKotlinType?.constructor?.declarationDescriptor?.reference()) {
                "Function reference parameter type has no originalKotlinType"
            }
        })
    }

    fun IrEnumEntry.reference(): KotlinEnumEntryReference {
        return KotlinEnumEntryReference(parentAsClass.reference(), name.asString())
    }

    private fun ClassifierDescriptor.reference(): KotlinTypeReference = when (this) {
        is ClassDescriptor -> reference()
        else -> error("Unsupported classifier descriptor: $this")
    }

    private fun DeclarationDescriptor.reference(): MemberParentReference = when (this) {
        is PackageFragmentDescriptor -> KotlinPackageReference(fqNameSafe.asString())
        is ClassDescriptor -> reference()
        else -> error("Unsupported declaration descriptor: $this")
    }

    private fun IrDeclarationParent.reference(): MemberParentReference = when (this) {
        is IrPackageFragment -> KotlinPackageReference(fqName.asString())
        is IrClass -> {
            if (isFileClass) {
                parent.reference()
            } else {
                KotlinTypeReference(parent.reference(), name.asString())
            }
        }
        else -> error("Unsupported declaration parent: $this")
    }

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
            SwiftPackModule.References(
                types = typeReferences.reverseReferences,
                properties = propertyReferences.reverseReferences,
                functions = functionReferences.reverseReferences,
                enumEntries = enumEntryReferences.reverseReferences,
            ),
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
        val packageReference: KotlinPackageReference = KotlinPackageReference.ROOT,
        private val types: MutableMap<KotlinTypeReference, TypeTransformScope> = mutableMapOf(),
        private val files: MutableMap<KotlinFileReference, FileTransformScope> = mutableMapOf(),
        private val properties: MutableMap<KotlinPropertyReference, PropertyTransformScope> = mutableMapOf(),
        private val functions: MutableMap<KotlinFunctionReference, FunctionTransformScope> = mutableMapOf(),
        private val enumEntries: MutableMap<KotlinEnumEntryReference, EnumEntryTransformScope> = mutableMapOf(),
    ) {
        fun type(name: String, builder: TypeTransformScope.() -> Unit) {
            val reference = KotlinTypeReference(packageReference, name)
            type(reference, builder)
        }

        fun type(reference: KotlinTypeReference, builder: TypeTransformScope.() -> Unit) {
            val scope = types.getOrPut(reference) { TypeTransformScope(reference) }
            scope.builder()
        }

        fun file(path: String, builder: FileTransformScope.() -> Unit) {
            val reference = KotlinFileReference(packageReference, path)
            file(reference, builder)
        }

        fun file(reference: KotlinFileReference, builder: FileTransformScope.() -> Unit) {
            val scope = files.getOrPut(reference) { FileTransformScope(reference) }
            scope.builder()
        }

        fun property(name: String, builder: PropertyTransformScope.() -> Unit) {
            val reference = KotlinPropertyReference(packageReference, name)
            property(reference, builder)
        }

        fun property(reference: KotlinPropertyReference, builder: PropertyTransformScope.() -> Unit) {
            when (val parent = reference.parent) {
                is KotlinPackageReference -> {
                    val scope = properties.getOrPut(reference) { PropertyTransformScope(reference) }
                    scope.builder()
                }
                is KotlinTypeReference -> type(parent) {
                    property(reference, builder)
                }
            }
        }

        fun function(name: String, builder: FunctionTransformScope.() -> Unit) {
            val reference = KotlinFunctionReference(packageReference, name, emptyList())
            function(reference, builder)
        }

        fun function(reference: KotlinFunctionReference, builder: FunctionTransformScope.() -> Unit) {
            when (val parent = reference.parent) {
                is KotlinPackageReference -> {
                    val scope = functions.getOrPut(reference) { FunctionTransformScope(reference) }
                    scope.builder()
                }
                is KotlinTypeReference -> type(parent) {
                    method(reference, builder)
                }
            }
        }

        fun enumEntry(reference: KotlinEnumEntryReference, builder: EnumEntryTransformScope.() -> Unit) {
            type(reference.enumType) {
                enumEntry(reference, builder)
            }
        }

        fun inPackage(packageName: String, builder: KobjcTransformScope.() -> Unit) {
            val scope = KobjcTransformScope(
                packageReference = packageReference.child(packageName),
                types = types,
                files = files,
                properties = properties,
                functions = functions,
                enumEntries = enumEntries,
            )
            scope.builder()
        }

        internal fun build(): KobjcTransforms {
            return KobjcTransforms(
                types = types.mapValues { it.value.build() },
                files = files.mapValues { it.value.build() },
                properties = properties.mapValues { it.value.build() },
                functions = functions.mapValues { it.value.build() },
                enumEntries = enumEntries.mapValues { it.value.build() },
            )
        }

        @KobjcScopeMarker
        class FileTransformScope(
            val reference: KotlinFileReference,
            private var hide: Boolean = false,
            private var remove: Boolean = false,
            private var rename: String? = null,
            private var bridge: String? = null,
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

            internal fun build(): KobjcTransforms.FileTransform {
                return KobjcTransforms.FileTransform(
                    reference = reference,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                    bridge = bridge,
                )
            }
        }

        @KobjcScopeMarker
        class TypeTransformScope(
            val reference: KotlinTypeReference,
            private var hide: Boolean = false,
            private var remove: Boolean = false,
            private var rename: String? = null,
            private var bridge: String? = null,
            private val properties: MutableMap<KotlinPropertyReference, PropertyTransformScope> = mutableMapOf(),
            private val methods: MutableMap<KotlinFunctionReference, FunctionTransformScope> = mutableMapOf(),
            private val enumEntries: MutableMap<KotlinEnumEntryReference, EnumEntryTransformScope> = mutableMapOf(),
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
                val reference = KotlinPropertyReference(reference, name)
                property(reference, builder)
            }

            fun property(reference: KotlinPropertyReference, builder: PropertyTransformScope.() -> Unit) {
                val scope = properties.getOrPut(reference) { PropertyTransformScope(reference) }
                scope.builder()
            }

            fun method(name: String, builder: FunctionTransformScope.() -> Unit) {
                val reference = KotlinFunctionReference(reference, name, emptyList())
                method(reference, builder)
            }

            fun method(reference: KotlinFunctionReference, builder: FunctionTransformScope.() -> Unit) {
                val scope = methods.getOrPut(reference) { FunctionTransformScope(reference) }
                scope.builder()
            }

            fun enumEntry(reference: KotlinEnumEntryReference, builder: EnumEntryTransformScope.() -> Unit) {
                val scope = enumEntries.getOrPut(reference) { EnumEntryTransformScope(reference) }
                scope.builder()
            }

            internal fun build(): KobjcTransforms.TypeTransform {
                return KobjcTransforms.TypeTransform(
                    reference = reference,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                    bridge = bridge,
                    properties = properties.mapValues { it.value.build() },
                    methods = methods.mapValues { it.value.build() },
                    enumEntries = enumEntries.mapValues { it.value.build() },
                )
            }
        }

        @KobjcScopeMarker
        class PropertyTransformScope(
            val reference: KotlinPropertyReference,
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
                    reference = reference,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                )
            }
        }

        @KobjcScopeMarker
        class FunctionTransformScope(
            val reference: KotlinFunctionReference,
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
                    reference = reference,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                )
            }
        }

        @KobjcScopeMarker
        class EnumEntryTransformScope(
            val reference: KotlinEnumEntryReference,
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

            internal fun build(): KobjcTransforms.EnumEntryTransform {
                return KobjcTransforms.EnumEntryTransform(
                    reference = reference,
                    hide = hide,
                    remove = remove,
                    rename = rename,
                )
            }
        }
    }

    object Config {
        private val storage = ThreadLocal<File?>()

        var outputDir: File?
            get() = storage.get()
            set(value) {
                storage.set(value)
            }
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
