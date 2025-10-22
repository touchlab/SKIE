package co.touchlab.skie.sir.builtin

import co.touchlab.skie.configuration.GlobalConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.getTypeParameter
import co.touchlab.skie.sir.element.toConformanceBound
import co.touchlab.skie.sir.element.toEqualityBound
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("PropertyName", "FunctionName")
class SirBuiltins(
    sirProvider: SirProvider,
    globalConfiguration: GlobalConfiguration,
) {

    val Swift = Modules.Swift(sirProvider)

    val SwiftUI = Modules.SwiftUI(sirProvider, Swift)

    val Foundation = Modules.Foundation(sirProvider, Swift)

    val _Concurrency = Modules._Concurrency(sirProvider, Swift)

    val Skie = Modules.Skie(sirProvider, globalConfiguration, sirProvider.skieModule, _Concurrency)

    val Combine = Modules.Combine(sirProvider)

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
                SirTypeParameter("Key", Hashable.defaultType.toConformanceBound())
                SirTypeParameter("Value")
            }

            val Set by Struct(superTypes = listOf(Hashable.defaultType)) {
                SirTypeParameter("Element", Hashable.defaultType.toConformanceBound())
            }

            val Void by Struct()
            val Never by Struct()
            val ObjectIdentifier by Struct()

            val UnsafeMutableRawPointer by Struct(superTypes = listOf(Hashable.defaultType))

            val String by Struct(superTypes = listOf(Hashable.defaultType))
            val StaticString by Struct()

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

            val SIMDScalar by Protocol()
            // TODO: There are more superTypes, but we don't need them
            val SIMD by Protocol(superTypes = listOf(Hashable.defaultType)) {
                SirTypeParameter(
                    name = "Scalar",
                    isPrimaryAssociatedType = true,
                )
                // TODO: There's also "MaskStorage" associatedtype, but we don't need it.
            }
            val SIMD4 by Struct(superTypes = listOf(SIMD.defaultType)) {
                SirTypeParameter(
                    "Scalar",
                    bounds = listOf(
                        SIMDScalar.defaultType.toConformanceBound(),
                    ),
                )
            }
        }

        class Foundation(sirProvider: SirProvider, swift: Swift) : ModuleBase() {

            override val declarationParent = sirProvider.getExternalModule("Foundation").builtInFile

            override val origin = SirClass.Origin.ExternalSwiftFramework

            val unichar by TypeAlias { swift.UInt16.defaultType }
        }

        class _Concurrency(sirProvider: SirProvider, swift: Swift) : ModuleBase() {

            override val declarationParent = sirProvider.getExternalModule("_Concurrency").builtInFile

            override val origin = SirClass.Origin.ExternalSwiftFramework

            val AsyncIteratorProtocol by Protocol {
                SirTypeParameter("Element")
            }

            val AsyncSequence by Protocol {
                SirTypeParameter("AsyncIterator", AsyncIteratorProtocol.defaultType.toConformanceBound())
                SirTypeParameter("Element", AsyncIteratorProtocol.getTypeParameter("Element").toTypeParameterUsage().toEqualityBound())
            }

            val CancellationError by Struct(
                superTypes = listOf(swift.Error.defaultType),
            )
        }

        class Combine(sirProvider: SirProvider) : ModuleBase() {

            override val declarationParent = sirProvider.getExternalModule("Combine").builtInFile

            override val origin = SirClass.Origin.ExternalSwiftFramework

            val Cancellable by Protocol()

            val CustomCombineIdentifierConvertible by Protocol()

            val Subscription by Protocol(
                superTypes = listOf(
                    Cancellable.defaultType,
                    CustomCombineIdentifierConvertible.defaultType,
                ),
            )
        }

        class SwiftUI(sirProvider: SirProvider, swift: Swift) : ModuleBase() {
            override val declarationParent = sirProvider.getExternalModule("SwiftUI").builtInFile

            override val origin = SirClass.Origin.ExternalSwiftFramework

            val View by Protocol()

            val EmptyView by Struct(
                superTypes = listOf(
                    View.defaultType
                )
            )

            val Binding by Struct {
                SirTypeParameter("Value")
            }

            val ObservableObject by Protocol(
                superTypes = listOf(
                    swift.AnyObject.defaultType,
                )
            ) {
                // TODO: Declared as: `associatedtype ObjectWillChangePublisher : Publisher = ObservableObjectPublisher where Self.ObjectWillChangePublisher.Failure == Never`
                SirTypeParameter("ObjectWillChangePublisher")
            }
        }

        class Skie(
            sirProvider: SirProvider,
            private val globalConfiguration: GlobalConfiguration,
            val module: SirModule.Skie,
            _concurrency: _Concurrency,
        ) : ModuleBase() {

            override val declarationParent: SirDeclarationParent = module.builtInFile

            override val origin: SirClass.Origin = SirClass.Origin.Generated

            val SkieSwiftFlowProtocol by lazy {
                sirProvider.getClassByFqName(SirFqName(module, "SkieSwiftFlowProtocol"))
            }
//             val SkieSwiftFlowProtocol by Protocol(
//                 superTypes = listOf(
//                     _concurrency.AsyncSequence.defaultType,
//                 ),
//             )

            val SkieSwiftFlowInternalProtocol by lazy {
                sirProvider.getClassByFqName(SirFqName(module, "SkieSwiftFlowInternalProtocol"))
            }

//             val SkieSwiftFlowInternalProtocol by Protocol {
//                 SirTypeParameter("Element")
//             }

            private fun RuntimeClass(
                superTypes: List<SirDeclaredSirType> = emptyList(),
                parent: SirDeclarationParent = declarationParent,
                nameOverride: String? = null,
                apply: (SirClass.() -> Unit) = { },
            ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, SirClass?>> =
                if (SkieConfigurationFlag.Feature_CoroutinesInterop in globalConfiguration.enabledFlags) {
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
