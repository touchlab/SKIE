package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.sir.element.SirClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

class KirClass(
    val descriptor: Descriptor,
    val name: ObjCExportNamer.ClassOrProtocolName,
    val parent: KirClassParent,
    val kind: Kind,
    superTypes: List<ReferenceKirType>,
    val isSealed: Boolean,
    val hasUnexposedSealedSubclasses: Boolean,
    val belongsToSkieKotlinRuntime: Boolean,
) : KirClassParent, KirBridgeableDeclaration<SirClass> {

    lateinit var oirClass: OirClass

    override val classes: MutableList<KirClass> = mutableListOf()

    var companionObject: KirClass? = null

    val callableDeclarations: MutableList<KirCallableDeclaration<*>> = mutableListOf()

    val enumEntries: MutableList<KirEnumEntry> = mutableListOf()

    val sealedSubclasses: MutableList<KirClass> = mutableListOf()

    val superTypes: MutableList<ReferenceKirType> = superTypes.toMutableList()

    val typeParameters: MutableList<KirTypeParameter> = mutableListOf()

    override val module: KirModule
        get() = parent.module

    override val configuration: KirConfiguration = KirConfiguration(parent.configuration)

    override val originalSirDeclaration: SirClass
        get() = oirClass.originalSirClass

    override val primarySirDeclaration: SirClass
        get() = oirClass.primarySirClass

    override var bridgedSirDeclaration: SirClass?
        get() = oirClass.bridgedSirClass
        set(value) {
            oirClass.bridgedSirClass = value
        }

    val originalSirClass: SirClass by ::originalSirDeclaration

    val primarySirClass: SirClass by ::primarySirDeclaration

    var bridgedSirClass: SirClass? by ::bridgedSirDeclaration

    init {
        parent.classes.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $descriptorString"

    private val descriptorString: String
        get() = when (descriptor) {
            is Descriptor.Class -> descriptor.value.toString()
            is Descriptor.File -> descriptor.value.name ?: descriptor.value.toString()
        }

    enum class Kind {
        Class, Interface, File, Enum, Object, CompanionObject
    }

    sealed interface Descriptor {

        data class Class(val value: ClassDescriptor) : Descriptor

        data class File(val value: SourceFile) : Descriptor
    }
}

val KirClass.classDescriptorOrNull: ClassDescriptor?
    get() = when (val descriptor = descriptor) {
        is KirClass.Descriptor.Class -> descriptor.value
        is KirClass.Descriptor.File -> null
    }

val KirClass.classDescriptorOrError: ClassDescriptor
    get() = classDescriptorOrNull ?: error("Class descriptor is not available for file classes. Was: $this")

val KirClass.sourceFileOrNull: SourceFile?
    get() = when (val descriptor = descriptor) {
        is KirClass.Descriptor.Class -> null
        is KirClass.Descriptor.File -> descriptor.value
    }

val KirClass.sourceFileOrError: SourceFile
    get() = sourceFileOrNull ?: error("Source file is not available for real classes. Was: $this")
