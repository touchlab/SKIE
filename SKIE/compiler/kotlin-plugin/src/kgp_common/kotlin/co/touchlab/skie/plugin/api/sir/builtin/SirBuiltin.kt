package co.touchlab.skie.plugin.api.sir.builtin

import co.touchlab.skie.plugin.api.sir.SirProvider
import co.touchlab.skie.plugin.api.sir.element.SirClass
import co.touchlab.skie.plugin.api.sir.element.SirDeclarationParent
import co.touchlab.skie.plugin.api.sir.element.SirModule
import co.touchlab.skie.plugin.api.sir.element.SirTypeAlias
import co.touchlab.skie.plugin.api.sir.element.SirTypeParameter
import co.touchlab.skie.plugin.api.sir.element.toTypeFromEnclosingTypeParameters
import co.touchlab.skie.plugin.api.sir.type.DeclaredSirType
import co.touchlab.skie.plugin.api.sir.type.SirType
import co.touchlab.skie.plugin.api.util.nsNumberKindClassIds
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("PropertyName", "FunctionName")
class SirBuiltins(
    kotlinModule: SirModule.Kotlin,
    skieModule: SirModule.Skie,
    sirProvider: SirProvider,
    namer: ObjCExportNamer,
) {

    val Swift = Modules.Swift(sirProvider)

    val Foundation = Modules.Foundation(sirProvider, Swift)

    val Kotlin = Modules.Kotlin(kotlinModule, Swift, Foundation, namer)

    val Skie = Modules.Skie(skieModule)

    object Modules {

        class Swift(sirProvider: SirProvider) : ModuleBase() {

            override val module = sirProvider.getExternalModule("Swift")

            val Hashable by Protocol()

            val AnyHashable by Struct(superTypes = listOf(Hashable.defaultType))

            val _ObjectiveCBridgeable by Protocol()

            val CaseIterable by Protocol()

            val AnyObject by Protocol()

            val AnyClass by Protocol(superTypes = listOf(AnyObject.defaultType))

            val Error by Protocol()

            val Array by Struct {
                SirTypeParameter(
                    name = "Element",
                    parent = this,
                )
            }

            val Dictionary by Struct {
                SirTypeParameter(
                    name = "Key",
                    parent = this,
                    bounds = listOf(Hashable.defaultType),
                )
                SirTypeParameter(
                    name = "Value",
                    parent = this,
                )
            }

            val Set by Struct(superTypes = listOf(Hashable.defaultType)) {
                SirTypeParameter(
                    name = "Element",
                    parent = this,
                    bounds = listOf(Hashable.defaultType),
                )
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
                SirTypeParameter(
                    name = "E",
                    parent = this,
                    bounds = listOf(swift.AnyObject.defaultType),
                )
            }

            val NSMutableArray by Class {
                SirTypeParameter(
                    name = "E",
                    parent = this,
                    bounds = listOf(swift.AnyObject.defaultType),
                )

                superTypes.add(
                    NSArray.toTypeFromEnclosingTypeParameters(typeParameters)
                )
            }

            val NSDictionary by Class(superTypes = listOf(NSObject.defaultType)) {
                SirTypeParameter(
                    name = "K",
                    parent = this,
                    bounds = listOf(swift.Hashable.defaultType, swift.AnyObject.defaultType),
                )
                SirTypeParameter(
                    name = "V",
                    parent = this,
                    bounds = listOf(swift.AnyObject.defaultType),
                )
            }

            val NSMutableDictionary by Class {
                SirTypeParameter(
                    name = "K",
                    parent = this,
                    bounds = listOf(swift.Hashable.defaultType, swift.AnyObject.defaultType),
                )
                SirTypeParameter(
                    name = "V",
                    parent = this,
                    bounds = listOf(swift.AnyObject.defaultType),
                )

                superTypes.add(
                    NSDictionary.toTypeFromEnclosingTypeParameters(typeParameters)
                )
            }

            val NSSet by Class(superTypes = listOf(NSObject.defaultType)) {
                SirTypeParameter(
                    name = "E",
                    parent = this,
                    bounds = listOf(swift.Hashable.defaultType, swift.AnyObject.defaultType),
                )
            }

            val NSMutableSet by Class {
                SirTypeParameter(
                    name = "E",
                    parent = this,
                    bounds = listOf(swift.Hashable.defaultType, swift.AnyObject.defaultType),
                )

                superTypes.add(
                    NSSet.toTypeFromEnclosingTypeParameters(typeParameters)
                )
            }
        }

        class Kotlin(
            override val module: SirModule.Kotlin,
            swift: Swift,
            foundation: Foundation,
            namer: ObjCExportNamer,
        ) : ModuleBase() {

            val Base by Class(nameOverride = namer.kotlinAnyName.swiftName, superTypes = listOf(foundation.NSObject.defaultType))

            val MutableSet by Class(nameOverride = namer.mutableSetName.swiftName) {
                SirTypeParameter(
                    name = "E",
                    parent = this,
                    bounds = listOf(swift.Hashable.defaultType, swift.AnyObject.defaultType),
                )

                superTypes.add(
                    foundation.NSMutableSet.toTypeFromEnclosingTypeParameters(typeParameters)
                )
            }

            val MutableMap by Class(nameOverride = namer.mutableMapName.swiftName) {
                SirTypeParameter(
                    name = "K",
                    parent = this,
                    bounds = listOf(swift.Hashable.defaultType, swift.AnyObject.defaultType),
                )
                SirTypeParameter(
                    name = "V",
                    parent = this,
                    bounds = listOf(swift.AnyObject.defaultType),
                )

                superTypes.add(
                    foundation.NSMutableDictionary.toTypeFromEnclosingTypeParameters(typeParameters)
                )
            }

            val Number by Class(nameOverride = namer.kotlinNumberName.swiftName, superTypes = listOf(foundation.NSNumber.defaultType))

            val nsNumberDeclarations by lazy {
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

            val allDeclarations: List<SirClass> by lazy {
                listOf(
                    Base,
                    MutableSet,
                    MutableMap,
                    Number,
                ) + nsNumberDeclarations.values
            }
        }

        class Skie(
            override val module: SirModule.Skie,
        ) : ModuleBase() {

            // The SkieSwiftFlow classes are only stubs (correct super types, type parameters and content are currently not needed)

            val SkieSwiftFlow by Class()

            val SkieSwiftSharedFlow by Class()

            val SkieSwiftMutableSharedFlow by Class()

            val SkieSwiftStateFlow by Class()

            val SkieSwiftMutableStateFlow by Class()

            val SkieSwiftOptionalFlow by Class()

            val SkieSwiftOptionalSharedFlow by Class()

            val SkieSwiftOptionalMutableSharedFlow by Class()

            val SkieSwiftOptionalStateFlow by Class()

            val SkieSwiftOptionalMutableStateFlow by Class()
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
