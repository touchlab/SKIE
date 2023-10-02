package co.touchlab.skie.sir.builtin

import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.util.nsNumberKindClassIds
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("PropertyName", "FunctionName")
class SirBuiltins(
    kotlinBuiltinsModule: SirModule.KotlinBuiltins,
    kotlinModule: SirModule.Kotlin,
    skieModule: SirModule.Skie,
    sirProvider: SirProvider,
    namer: ObjCExportNamer,
) {

    val Swift = Modules.Swift(sirProvider)

    val Foundation = Modules.Foundation(sirProvider, Swift)

    val Stdlib = Modules.KotlinBuiltins(kotlinBuiltinsModule, Swift, Foundation, namer)

    val Kotlin = Modules.Kotlin(kotlinModule)

    val Skie = Modules.Skie(skieModule, Foundation)

    object Modules {

        class Swift(sirProvider: SirProvider) : ModuleBase() {

            override val module = sirProvider.getExternalModule("Swift")

            val Hashable by Protocol {
                isInherentlyHashable = true
            }

            val AnyHashable by Struct(superTypes = listOf(Hashable.defaultType))

            val _ObjectiveCBridgeable by Protocol {
                SirTypeParameter("_ObjectiveCType")
            }

            val CaseIterable by Protocol()

            val AnyObject by Protocol()

            val AnyClass by Protocol(superTypes = listOf(AnyObject.defaultType))

            val Error by Protocol()

            val Array by Struct {
                SirTypeParameter("Element")
            }

            val Dictionary by Struct {
                SirTypeParameter("Key", Hashable.defaultType)
                SirTypeParameter("Value")
            }

            val Set by Struct(superTypes = listOf(Hashable.defaultType)) {
                SirTypeParameter("Element", Hashable.defaultType)
            }

            val Void by Struct(isPrimitive = true)

            val UnsafeMutableRawPointer by Struct(superTypes = listOf(Hashable.defaultType))

            val String by Struct(superTypes = listOf(Hashable.defaultType))

            val Bool by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)

            val Int by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val Int8 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val Int16 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val Int32 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val Int64 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)

            val UInt by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val UInt8 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val UInt16 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val UInt32 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val UInt64 by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)

            val Float by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
            val Double by Struct(superTypes = listOf(Hashable.defaultType), isPrimitive = true)
        }

        class Foundation(sirProvider: SirProvider, swift: Swift) : ModuleBase() {

            override val module = sirProvider.getExternalModule("Foundation")

            val unichar by TypeAlias { swift.UInt16.defaultType }

            val NSObject by Class(superTypes = listOf(swift.Hashable.defaultType, swift.AnyObject.defaultType))

            val NSString by Class(superTypes = listOf(NSObject.defaultType))

            val NSValue by Class(superTypes = listOf(NSObject.defaultType))

            val NSNumber by Class(superTypes = listOf(NSValue.defaultType))

            val NSArray by Class(superTypes = listOf(NSObject.defaultType)) {
                SirTypeParameter("E", swift.AnyObject.defaultType)
            }

            val NSMutableArray by Class {
                SirTypeParameter("E", swift.AnyObject.defaultType)

                superTypes.add(
                    NSArray.toTypeFromEnclosingTypeParameters(typeParameters),
                )
            }

            val NSDictionary by Class(superTypes = listOf(NSObject.defaultType)) {
                SirTypeParameter("K", swift.Hashable.defaultType, swift.AnyObject.defaultType)
                SirTypeParameter("V", swift.AnyObject.defaultType)
            }

            val NSMutableDictionary by Class {
                SirTypeParameter("K", swift.Hashable.defaultType, swift.AnyObject.defaultType)
                SirTypeParameter("V", swift.AnyObject.defaultType)

                superTypes.add(
                    NSDictionary.toTypeFromEnclosingTypeParameters(typeParameters),
                )
            }

            val NSSet by Class(superTypes = listOf(NSObject.defaultType)) {
                SirTypeParameter("E", swift.Hashable.defaultType, swift.AnyObject.defaultType)
            }

            val NSMutableSet by Class {
                SirTypeParameter("E", swift.Hashable.defaultType, swift.AnyObject.defaultType)

                superTypes.add(
                    NSSet.toTypeFromEnclosingTypeParameters(typeParameters),
                )
            }
        }

        class KotlinBuiltins(
            override val module: SirModule.KotlinBuiltins,
            swift: Swift,
            foundation: Foundation,
            namer: ObjCExportNamer,
        ) : ModuleBase() {

            val Base by Class(nameOverride = namer.kotlinAnyName.swiftName, superTypes = listOf(foundation.NSObject.defaultType))

            val MutableSet by Class(nameOverride = namer.mutableSetName.swiftName) {
                SirTypeParameter("E", swift.Hashable.defaultType, swift.AnyObject.defaultType)

                superTypes.add(
                    foundation.NSMutableSet.toTypeFromEnclosingTypeParameters(typeParameters),
                )
            }

            val MutableMap by Class(nameOverride = namer.mutableMapName.swiftName) {
                SirTypeParameter("K", swift.Hashable.defaultType, swift.AnyObject.defaultType)
                SirTypeParameter("V", swift.AnyObject.defaultType)

                superTypes.add(
                    foundation.NSMutableDictionary.toTypeFromEnclosingTypeParameters(typeParameters),
                )
            }

            val Number by Class(nameOverride = namer.kotlinNumberName.swiftName, superTypes = listOf(foundation.NSNumber.defaultType))

            val nsNumberDeclarations =
                nsNumberKindClassIds().associateWith {
                    namer.kotlinNumberName
                    SirClass(
                        simpleName = namer.numberBoxName(it).swiftName,
                        kind = SirClass.Kind.Class,
                        parent = module,
                        superTypes = listOf(Number.defaultType),
                    )
                }
        }

        class Kotlin(
            override val module: SirModule.Kotlin,
        ) : ModuleBase()

        class Skie(
            override val module: SirModule.Skie,
            foundation: Foundation,
        ) : ModuleBase() {

            // The SkieSwiftFlow classes are only stubs (correct super types, and content are currently not needed)

            val SkieSwiftFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftSharedFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftMutableSharedFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftStateFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftMutableStateFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalSharedFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalMutableSharedFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalStateFlow by Class {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalMutableStateFlow by Class {
                SirTypeParameter("T")
            }
        }

        abstract class ModuleBase {

            abstract val module: SirDeclarationParent

            protected fun Class(
                superTypes: List<DeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = module,
                nameOverride: String? = null,
                apply: (SirClass.() -> Unit) = { },
            ) = ClassDeclarationPropertyProvider(
                kind = SirClass.Kind.Class,
                parent = parent,
                superTypes = superTypes,
                nameOverride = nameOverride,
                apply = apply,
            )

            protected fun Protocol(
                superTypes: List<DeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = module,
                apply: (SirClass.() -> Unit) = { },
            ) = ClassDeclarationPropertyProvider(
                kind = SirClass.Kind.Protocol,
                parent = parent,
                superTypes = superTypes,
                apply = apply,
            )

            protected fun Struct(
                superTypes: List<DeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = module,
                isPrimitive: Boolean = false,
                apply: (SirClass.() -> Unit) = { },
            ) = ClassDeclarationPropertyProvider(
                kind = SirClass.Kind.Struct,
                parent = parent,
                isPrimitive = isPrimitive,
                superTypes = superTypes,
                apply = apply,
            )

            protected fun TypeAlias(
                parent: SirDeclarationParent = module,
                apply: (SirTypeAlias.() -> Unit) = { },
                typeFactory: ((SirTypeAlias) -> SirType),
            ) = TypeAliasDeclarationPropertyProvider(
                parent = parent,
                apply = apply,
                typeFactory = typeFactory,
            )

            inner class ClassDeclarationPropertyProvider(
                private val kind: SirClass.Kind,
                private val parent: SirDeclarationParent,
                private val isPrimitive: Boolean = false,
                private val superTypes: List<DeclaredSirType> = emptyList(),
                private val apply: (SirClass.() -> Unit) = { },
                private val nameOverride: String? = null,
            ) : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SirClass>> {

                override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, SirClass> =
                    ClassDeclarationProperty(
                        name = nameOverride ?: property.name,
                        kind = kind,
                        isPrimitive = isPrimitive,
                        parent = parent,
                        superTypes = superTypes,
                        apply = apply,
                    )
            }

            private inner class ClassDeclarationProperty(
                name: String,
                kind: SirClass.Kind,
                isPrimitive: Boolean,
                parent: SirDeclarationParent,
                superTypes: List<DeclaredSirType>,
                apply: (SirClass.() -> Unit) = { },
            ) : ReadOnlyProperty<Any?, SirClass> {

                private val value = SirClass(
                    simpleName = name,
                    kind = kind,
                    isPrimitive = isPrimitive,
                    parent = parent,
                    superTypes = superTypes,
                ).apply(apply)

                override fun getValue(thisRef: Any?, property: KProperty<*>): SirClass =
                    value
            }

            inner class TypeAliasDeclarationPropertyProvider(
                private val parent: SirDeclarationParent,
                private val typeFactory: ((SirTypeAlias) -> SirType),
                private val apply: (SirTypeAlias.() -> Unit) = { },
                private val nameOverride: String? = null,
            ) : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SirTypeAlias>> {

                override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, SirTypeAlias> =
                    TypeAliasDeclarationProperty(
                        name = nameOverride ?: property.name,
                        parent = parent,
                        apply = apply,
                        typeFactory = typeFactory,
                    )
            }

            private inner class TypeAliasDeclarationProperty(
                name: String,
                parent: SirDeclarationParent,
                apply: (SirTypeAlias.() -> Unit) = { },
                typeFactory: ((SirTypeAlias) -> SirType),
            ) : ReadOnlyProperty<Any?, SirTypeAlias> {

                private val value = SirTypeAlias(
                    simpleName = name,
                    parent = parent,
                    typeFactory = typeFactory,
                ).apply(apply)

                override fun getValue(thisRef: Any?, property: KProperty<*>): SirTypeAlias =
                    value
            }
        }
    }
}
