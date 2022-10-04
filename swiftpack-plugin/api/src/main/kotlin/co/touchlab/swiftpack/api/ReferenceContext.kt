package co.touchlab.swiftpack.api

import co.touchlab.swiftpack.spec.ApiTransform
import co.touchlab.swiftpack.spec.KotlinClass
import co.touchlab.swiftpack.spec.KotlinEnumEntry
import co.touchlab.swiftpack.spec.KotlinFile
import co.touchlab.swiftpack.spec.KotlinFunction
import co.touchlab.swiftpack.spec.KotlinMemberParent
import co.touchlab.swiftpack.spec.KotlinPackage
import co.touchlab.swiftpack.spec.KotlinProperty
import co.touchlab.swiftpack.spec.KotlinSymbol
import co.touchlab.swiftpack.spec.KotlinType
import co.touchlab.swiftpack.spec.KotlinTypeParameter
import co.touchlab.swiftpack.spec.KotlinTypeParameterParent
import co.touchlab.swiftpack.spec.SwiftPackModule2
import co.touchlab.swiftpack.spec.SwiftPackModule2.Companion.write
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.backend.konan.serialization.KonanIdSignaturer
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerIr
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.KotlinMangler
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isFileClass
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.types.FlexibleType
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.WrappedType
import org.jetbrains.kotlin.types.KotlinType as CompilerKotlinType

interface IrReferenceContext {
    fun IrClass.reference(): KotlinClass

    fun IrProperty.reference(): KotlinProperty

    fun IrFunction.reference(): KotlinFunction

    fun IrEnumEntry.reference(): KotlinEnumEntry
}

interface DescriptorReferenceContext {
    fun ClassDescriptor.classReference(): KotlinClass

    fun ClassDescriptor.enumEntryReference(): KotlinEnumEntry

    fun CompilerKotlinType.reference(): KotlinType<*>

    fun PropertyDescriptor.reference(): KotlinProperty

    fun FunctionDescriptor.reference(): KotlinFunction
}

interface ReferenceContext: IrReferenceContext, DescriptorReferenceContext

internal interface InternalReferenceContext: ReferenceContext {
    val references: Map<KotlinSymbol.Id, KotlinSymbol<*>>

    val symbols: List<KotlinSymbol<*>>
}

internal class DefaultReferenceContext(
    private val compatibleMode: Boolean = true
): InternalReferenceContext {
    private val irMangler: KotlinMangler.IrMangler
        get() = KonanManglerIr

    private val descriptorMangler: KotlinMangler.DescriptorMangler
        get() = KonanManglerDesc

    private val signaturer = KonanIdSignaturer(descriptorMangler)

    private val mutableReferences = mutableMapOf<KotlinSymbol.Id, KotlinSymbol<*>>()
    override val references: Map<KotlinSymbol.Id, KotlinSymbol<*>>
        get() = mutableReferences

    override val symbols: List<KotlinSymbol<*>>
        get() = references.values.toList()

    override fun IrClass.reference(): KotlinClass = with(irMangler) {
        getReference(KotlinClass.Id(mangleString(compatibleMode))) { id ->
            KotlinClass(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Class ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrProperty.reference(): KotlinProperty = with(irMangler) {
        getReference(KotlinProperty.Id(mangleString(compatibleMode))) { id ->
            KotlinProperty(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Property ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrFunction.reference(): KotlinFunction = with(irMangler) {
        getReference(KotlinFunction.Id(mangleString(compatibleMode))) { id ->
            KotlinFunction(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Function ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrEnumEntry.reference(): KotlinEnumEntry = with (irMangler) {
        getReference(KotlinEnumEntry.Id(mangleString(compatibleMode))) { id ->
            KotlinEnumEntry(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Enum entry ${this@reference} has no signature"
                },
            )
        }
    }

    // private fun IrDeclarationParent.reference(): KotlinMemberParent<*> =
    //     when (this) {
    //         is IrPackageFragment -> getReference(KotlinPackage.Id(fqName.asString())) { id ->
    //             KotlinPackage(id = id, fqName.asString())
    //         }
    //         is IrClass -> {
    //             if (isFileClass) {
    //                 parent.reference()
    //             } else {
    //                 reference()
    //             }
    //         }
    //         else -> error("Unsupported declaration parent: $this")
    //     }

    // private fun IrDeclarationParent.enumClassReference(): KotlinClass =
    //     if (this is IrClass && this.isEnumClass) {
    //         reference()
    //     } else {
    //         error("Not an enum class: $this")
    //     }

    override fun ClassDescriptor.classReference(): KotlinClass = with (descriptorMangler) {
        getReference(KotlinClass.Id(mangleString(compatibleMode))) { id ->
            KotlinClass(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@classReference)) {
                    "Class ${this@classReference} has no signature"
                },
            )
        }
    }

    override fun ClassDescriptor.enumEntryReference(): KotlinEnumEntry = with(descriptorMangler) {
        getReference(KotlinEnumEntry.Id(mangleString(compatibleMode))) { id ->
            KotlinEnumEntry(
                id = id,
                signature = requireNotNull(signaturer.composeEnumEntrySignature(this@enumEntryReference)) {
                    "Enum entry ${this@enumEntryReference} has no signature"
                },
            )
        }
    }

    override fun CompilerKotlinType.reference(): KotlinType<*> = with(descriptorMangler) {
        val declarationDescriptor = requireNotNull(constructor.declarationDescriptor) {
            "Type $this has no declaration descriptor"
        }
        getReference(KotlinClass.Id(declarationDescriptor.mangleString(compatibleMode))) { id ->
            KotlinClass(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(declarationDescriptor)) {
                    "Class ${this@reference} has no signature"
                },
            )
        }
    }

    override fun PropertyDescriptor.reference(): KotlinProperty = with(descriptorMangler) {
        getReference(KotlinProperty.Id(mangleString(compatibleMode))) { id ->
            KotlinProperty(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@reference)) {
                    "Property ${this@reference} has no signature"
                },
            )
        }
    }

    override fun FunctionDescriptor.reference(): KotlinFunction = with(descriptorMangler) {
        getReference(KotlinFunction.Id(mangleString(compatibleMode))) { id ->
            KotlinFunction(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@reference)) {
                    "Function ${this@reference} has no signature"
                },
            )
        }
    }

    // private fun DeclarationDescriptor.reference(parentId: KotlinSymbol.Id?): KotlinMemberParent<*> = when (this) {
    //     is PackageFragmentDescriptor -> getReference(KotlinPackage.Id(fqName.asString())) { id ->
    //         KotlinPackage(id = id, packageName = fqName.asString())
    //     }
    //     is ClassDescriptor -> classReference(parentId as? KotlinMemberParent.Id)
    //     is TypeParameterDescriptor -> with(descriptorMangler) {
    //         getReference(KotlinTypeParameter.Id(mangleString(compatibleMode))) { id ->
    //             KotlinTypeParameter(
    //                 id = id,
    //                 parentId = requireNotNull((parentId as? KotlinTypeParameterParent.Id) ?: containingDeclaration.reference(id).id  as? KotlinTypeParameterParent.Id) {
    //                     "Couldn't resolve type parameter parent!"
    //                 },
    //                 typeName = name.asString(),
    //             )
    //         }
    //     }
    //     else -> error("Unsupported declaration parent: $this")
    // }

    // private fun DeclarationDescriptor.enumClassReference(): KotlinClass = if (this is ClassDescriptor && kind == ClassKind.ENUM_CLASS) {
    //     classReference()
    // } else {
    //     error("Not an enum class: $this")
    // }

    private fun <S: KotlinSymbol<ID>, ID: KotlinSymbol.Id> getReference(id: ID, symbolFactory: (ID) -> S): S {
        @Suppress("UNCHECKED_CAST")
        return mutableReferences.getOrPut(id) {
            symbolFactory(id)
        } as S
    }
}

interface TransformContext {
    fun KotlinClass.applyTransform(transform: KotlinClassTransformScope.() -> Unit): KotlinClass

    fun KotlinProperty.applyTransform(transform: KotlinPropertyTransformScope.() -> Unit): KotlinProperty

    fun KotlinFunction.applyTransform(transform: KotlinFunctionTransformScope.() -> Unit): KotlinFunction

    fun KotlinFile.applyTransform(transform: KotlinFileTransformScope.() -> Unit): KotlinFile

    @DslMarker
    annotation class TransformScopeMarker

    @TransformScopeMarker
    interface KotlinClassTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)

        fun rename(newSwiftName: ApiTransform.TypeTransform.Rename)

        fun bridge(bridge: ApiTransform.TypeTransform.Bridge)

        fun bridge(swiftType: String)
    }

    @TransformScopeMarker
    interface KotlinPropertyTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)
    }

    @TransformScopeMarker
    interface KotlinFunctionTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)
    }

    @TransformScopeMarker
    interface KotlinFileTransformScope {
        fun remove()

        fun hide()

        fun rename(newSwiftName: String)

        fun bridge(swiftType: String)
    }
}

internal interface InternalTransformContext: TransformContext {
    val transforms: List<ApiTransform>
}

internal class DefaultTransformContext: InternalTransformContext {
    private val mutableTransforms = mutableMapOf<KotlinSymbol.Id, ApiTransformScope<*>>()
    override val transforms: List<ApiTransform>
        get() = mutableTransforms.values.map { it.build() }

    override fun KotlinClass.applyTransform(transform: TransformContext.KotlinClassTransformScope.() -> Unit): KotlinClass {
        getScope(id) {
            KotlinClassTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinProperty.applyTransform(transform: TransformContext.KotlinPropertyTransformScope.() -> Unit): KotlinProperty {
        getScope(id) {
            KotlinPropertyTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinFunction.applyTransform(transform: TransformContext.KotlinFunctionTransformScope.() -> Unit): KotlinFunction {
        getScope(id) {
            KotlinFunctionTransformScope(id)
        }.transform()
        return this
    }

    override fun KotlinFile.applyTransform(transform: TransformContext.KotlinFileTransformScope.() -> Unit): KotlinFile {
        getScope(id) {
            KotlinFileTransformScope(id)
        }.transform()
        return this
    }

    private fun <SCOPE: ApiTransformScope<ID>, ID: KotlinSymbol.Id> getScope(id: ID, scopeFactory: (ID) -> SCOPE): SCOPE {
        @Suppress("UNCHECKED_CAST")
        return mutableTransforms.getOrPut(id) {
            scopeFactory(id)
        } as SCOPE
    }

    private interface ApiTransformScope<ID: KotlinSymbol.Id> {
        fun build(): ApiTransform
    }

    private class KotlinClassTransformScope(
        private val classId: KotlinClass.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: ApiTransform.TypeTransform.Rename? = null,
        private var bridge: ApiTransform.TypeTransform.Bridge? = null,
    ): TransformContext.KotlinClassTransformScope, ApiTransformScope<KotlinClass.Id> {
         override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename(
                ApiTransform.TypeTransform.Rename(
                    kind = ApiTransform.TypeTransform.Rename.Kind.ABSOLUTE,
                    action = ApiTransform.TypeTransform.Rename.Action.Replace(newSwiftName),
                )
            )
        }

        override fun rename(rename: ApiTransform.TypeTransform.Rename) {
            this.rename = rename
        }

        override fun bridge(swiftType: String) {
            bridge(ApiTransform.TypeTransform.Bridge.Absolute(swiftType))
        }

        override fun bridge(bridge: ApiTransform.TypeTransform.Bridge) {
            this.bridge = bridge
        }

        override fun build(): ApiTransform = ApiTransform.TypeTransform(
            typeId = classId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename,
            bridge = bridge,
        )
    }

    private class KotlinPropertyTransformScope(
        private val propertyId: KotlinProperty.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
    ): TransformContext.KotlinPropertyTransformScope, ApiTransformScope<KotlinProperty.Id> {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }

        override fun build(): ApiTransform = ApiTransform.PropertyTransform(
            propertyId = propertyId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename,
        )
    }

    private class KotlinFunctionTransformScope(
        private val functionId: KotlinFunction.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
    ): TransformContext.KotlinFunctionTransformScope, ApiTransformScope<KotlinFunction.Id> {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }

        override fun build(): ApiTransform = ApiTransform.FunctionTransform(
            functionId = functionId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename,
        )
    }

    private class KotlinFileTransformScope(
        private val fileId: KotlinFile.Id,
        private var isRemoved: Boolean = false,
        private var isHidden: Boolean = false,
        private var rename: String? = null,
        private var bridge: String? = null,
    ): TransformContext.KotlinFileTransformScope, ApiTransformScope<KotlinFile.Id> {
        override fun remove() {
            isRemoved = true
        }

        override fun hide() {
            isHidden = true
        }

        override fun rename(newSwiftName: String) {
            rename = newSwiftName
        }

        override fun bridge(swiftType: String) {
            bridge = swiftType
        }

        override fun build(): ApiTransform = ApiTransform.FileTransform(
            fileId = fileId,
            hide = isHidden,
            remove = isRemoved,
            rename = rename?.let { ApiTransform.TypeTransform.Rename.Action.Replace(it) },
            bridge = bridge,
        )
    }
}

class SwiftPackModuleBuilder2 internal constructor(
    private val moduleName: String,
    private val referenceContext: InternalReferenceContext = DefaultReferenceContext(),
    private val templateVariableContext: InternalTemplateVariableContext = DefaultTemplateVariableContext(),
    private val transformContext: InternalTransformContext = DefaultTransformContext(),
): ReferenceContext by referenceContext, TemplateVariableContext by templateVariableContext, TransformContext by transformContext {

    private val mutableFiles = mutableSetOf<FileSpec>()
    val files: Set<FileSpec> get() = mutableFiles

    fun file(name: String, contents: FileSpec.Builder.() -> Unit): FileSpec {
        val builder = FileSpec.builder(name)
        builder.contents()
        val file = builder.build()
        mutableFiles.add(file)
        return file
    }

    fun addFile(file: FileSpec): SwiftPackModuleBuilder2 {
        mutableFiles.add(file)
        return this
    }

    fun build(): SwiftPackModule2 {
        return SwiftPackModule2(
            name = SwiftPackModule2.Name.Simple(moduleName),
            templateVariables = templateVariableContext.variables.toList(),
            symbols = referenceContext.symbols,
            files = files.map {
                SwiftPackModule2.TemplateFile(
                    name = it.name,
                    contents = it.toString(),
                )
            }.sortedBy { it.name },
            transforms = transformContext.transforms,
        )
    }
}

fun buildSwiftPackModule2(
    moduleName: String = "main",
    writeToOutputDir: Boolean = true,
    block: SwiftPackModuleBuilder2.() -> Unit
): SwiftPackModule2 {
    val context = SwiftPackModuleBuilder2(moduleName)
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
