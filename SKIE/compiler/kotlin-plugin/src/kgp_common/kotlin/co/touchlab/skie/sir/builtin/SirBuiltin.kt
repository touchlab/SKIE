package co.touchlab.skie.sir.builtin

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("PropertyName", "FunctionName")
class SirBuiltins(
    sirProvider: SirProvider,
    skieConfiguration: SkieConfiguration,
) {

    val Swift = Modules.Swift(sirProvider)

    val Foundation = Modules.Foundation(sirProvider, Swift)

    val Skie = Modules.Skie(skieConfiguration, sirProvider.skieModule)

    object Modules {

        class Swift(sirProvider: SirProvider) : ModuleBase() {

            override val declarationParent = sirProvider.getExternalModule("Swift").builtInFile

            override val origin: SirClass.Origin = SirClass.Origin.ExternalSwiftFramework

            val Hashable by Protocol {
                isInherentlyHashable = true
            }

            val AnyHashable by Struct(superTypes = listOf(Hashable.defaultType))

            val _ObjectiveCBridgeable by Protocol {
                SirTypeParameter("_ObjectiveCType")
            }

            val CaseIterable by Protocol()

            val AnyObject by Protocol {
                isAlwaysAReference = true
            }

            val Optional by Protocol {
                SirTypeParameter("Wrapped")
            }

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

            val Void by Struct()

            val UnsafeMutableRawPointer by Struct(superTypes = listOf(Hashable.defaultType))

            val String by Struct(superTypes = listOf(Hashable.defaultType))

            val Bool by Struct(superTypes = listOf(Hashable.defaultType))

            val Int by Struct(superTypes = listOf(Hashable.defaultType))
            val Int8 by Struct(superTypes = listOf(Hashable.defaultType))
            val Int16 by Struct(superTypes = listOf(Hashable.defaultType))
            val Int32 by Struct(superTypes = listOf(Hashable.defaultType))
            val Int64 by Struct(superTypes = listOf(Hashable.defaultType))

            val UInt by Struct(superTypes = listOf(Hashable.defaultType))
            val UInt8 by Struct(superTypes = listOf(Hashable.defaultType))
            val UInt16 by Struct(superTypes = listOf(Hashable.defaultType))
            val UInt32 by Struct(superTypes = listOf(Hashable.defaultType))
            val UInt64 by Struct(superTypes = listOf(Hashable.defaultType))

            val Float by Struct(superTypes = listOf(Hashable.defaultType))
            val Double by Struct(superTypes = listOf(Hashable.defaultType))
        }

        class Foundation(sirProvider: SirProvider, swift: Swift) : ModuleBase() {

            override val declarationParent = sirProvider.getExternalModule("Foundation").builtInFile

            override val origin = SirClass.Origin.ExternalSwiftFramework

            val unichar by TypeAlias { swift.UInt16.defaultType }
        }

        class Skie(
            private val skieConfiguration: SkieConfiguration,
            val module: SirModule.Skie,
        ) : ModuleBase() {

            override val declarationParent: SirDeclarationParent = module.builtInFile

            override val origin: SirClass.Origin = SirClass.Origin.Generated

            // The SkieSwiftFlow classes are only stubs (correct super types, and content are currently not needed)

            val SkieSwiftFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftSharedFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftMutableSharedFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftStateFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftMutableStateFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalSharedFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalMutableSharedFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalStateFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            val SkieSwiftOptionalMutableStateFlow by RuntimeClass {
                SirTypeParameter("T")
            }

            private fun RuntimeClass(
                superTypes: List<SirDeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = declarationParent,
                nameOverride: String? = null,
                apply: (SirClass.() -> Unit) = { },
            ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SirClass?>> =
                if (SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags) {
                    ClassDeclarationPropertyProvider(
                        kind = SirClass.Kind.Class,
                        parent = parent,
                        superTypes = superTypes,
                        nameOverride = nameOverride,
                        apply = apply,
                    )
                } else {
                    NoClassDeclarationPropertyProvider
                }
        }

        abstract class ModuleBase {

            abstract val declarationParent: SirDeclarationParent

            abstract val origin: SirClass.Origin

            protected fun Class(
                superTypes: List<SirDeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = declarationParent,
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
                superTypes: List<SirDeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = declarationParent,
                apply: (SirClass.() -> Unit) = { },
            ) = ClassDeclarationPropertyProvider(
                kind = SirClass.Kind.Protocol,
                parent = parent,
                superTypes = superTypes,
                apply = apply,
            )

            protected fun Struct(
                superTypes: List<SirDeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = declarationParent,
                apply: (SirClass.() -> Unit) = { },
            ) = ClassDeclarationPropertyProvider(
                kind = SirClass.Kind.Struct,
                parent = parent,
                superTypes = superTypes,
                apply = apply,
            )

            protected fun TypeAlias(
                parent: SirDeclarationParent = declarationParent,
                apply: (SirTypeAlias.() -> Unit) = { },
                typeFactory: ((SirTypeAlias) -> SirType),
            ) = TypeAliasDeclarationPropertyProvider(
                parent = parent,
                apply = apply,
                typeFactory = typeFactory,
            )

            protected inner class ClassDeclarationPropertyProvider(
                private val kind: SirClass.Kind,
                private val parent: SirDeclarationParent,
                private val superTypes: List<SirDeclaredSirType> = emptyList(),
                private val apply: (SirClass.() -> Unit) = { },
                private val nameOverride: String? = null,
            ) : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SirClass>> {

                override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, SirClass> =
                    ClassDeclarationProperty(
                        name = nameOverride ?: property.name,
                        kind = kind,
                        parent = parent,
                        superTypes = superTypes,
                        apply = apply,
                    )
            }

            protected object NoClassDeclarationPropertyProvider : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SirClass?>> {

                override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, SirClass?> =
                    NoClassDeclarationProperty

                private object NoClassDeclarationProperty : ReadOnlyProperty<Any?, SirClass?> {

                    override fun getValue(thisRef: Any?, property: KProperty<*>): SirClass? =
                        null
                }
            }

            private inner class ClassDeclarationProperty(
                name: String,
                kind: SirClass.Kind,
                parent: SirDeclarationParent,
                superTypes: List<SirDeclaredSirType>,
                apply: (SirClass.() -> Unit) = { },
            ) : ReadOnlyProperty<Any?, SirClass> {

                private val value = SirClass(
                    baseName = name,
                    kind = kind,
                    parent = parent,
                    superTypes = superTypes,
                    origin = origin,
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
                    baseName = name,
                    parent = parent,
                    typeFactory = typeFactory,
                ).apply(apply)

                override fun getValue(thisRef: Any?, property: KProperty<*>): SirTypeAlias =
                    value
            }
        }
    }
}
