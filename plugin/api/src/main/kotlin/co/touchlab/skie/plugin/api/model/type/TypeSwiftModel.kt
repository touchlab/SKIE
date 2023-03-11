package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName

@JvmInline
value class ObjcFqName(
    val name: String,
) {
    fun asString(): String = name

    override fun toString(): String = asString()
}

sealed interface SwiftFqName {
    val name: String

    sealed interface Local: SwiftFqName {

        data class TopLevel(
            override val name: String,
        ): Local {
            override fun asString(nestingSeparator: String): String = name

            override fun toString(): String = asString()
        }

        data class Nested(
            val parent: Local,
            override val name: String,
        ): Local {
            override fun asString(nestingSeparator: String): String = listOf(parent.asString(), name).joinToString(nestingSeparator)

            override fun toString(): String = asString()
        }

        // TODO: Should this be the only place that knows about `qualifiedLocalTypeName`?
        override fun toSwiftPoetName(): DeclaredTypeName = DeclaredTypeName.qualifiedLocalTypeName(asString())

        override fun nested(name: String): Nested = Nested(this, name)
    }

    sealed interface External: SwiftFqName {
        data class TopLevel(
            val module: String,
            override val name: String,
        ): External {
            override fun asString(nestingSeparator: String): String = listOf(module, name).joinToString(nestingSeparator)

            override fun toString(): String = asString()
        }

        data class Nested(
            val parent: External,
            override val name: String,
        ): External {
            override fun asString(nestingSeparator: String): String = listOf(parent.asString(), name).joinToString(nestingSeparator)

            override fun toString(): String = asString()
        }

        override fun toSwiftPoetName(): DeclaredTypeName = DeclaredTypeName.qualifiedTypeName(asString())

        override fun nested(name: String): Nested = Nested(this, name)
    }

    fun nested(name: String): SwiftFqName

    fun asString(nestingSeparator: String = "."): String

    fun toSwiftPoetName(): DeclaredTypeName
}


// Split to `SwiftIrDeclaration.Name` and `SirType.Reference`
// sealed interface SwiftFqName {
//     val name: String
//
//     fun toSwiftPoetName(): TypeName
//
//     companion object {
//
//         private fun DeclaredTypeName.parametrizedByIfNotEmpty(typeArguments: List<SwiftFqName>) = if (typeArguments.isEmpty()) {
//             this
//         } else {
//             this.parameterizedBy(*typeArguments.map { it.toSwiftPoetName() }.toTypedArray())
//         }
//     }
//
//     data class Parametrized(
//         val base: SwiftFqName.NominalType,
//         val typeArguments: List<SwiftFqName>,
//     ): SwiftFqName {
//         override val name: String
//             get() = TODO("Not yet implemented")
//
//         override fun toSwiftPoetName(): TypeName = base.toSwiftPoetName().parametrizedByIfNotEmpty(typeArguments)
//     }
//
//     sealed interface NominalType: SwiftFqName {
//         override fun toSwiftPoetName(): DeclaredTypeName
//
//         fun withTypeArguments(typeArguments: List<SwiftFqName>): Parametrized = Parametrized(this, typeArguments)
//
//         // fun prefixedWith(prefix: String): NominalType
//     }
//
//     sealed interface Local: NominalType {
//         val typealiasName: String
//
//         val originalSpec: DeclaredTypeName
//
//         override fun toSwiftPoetName(): DeclaredTypeName {
//             return DeclaredTypeName.qualifiedLocalTypeName("${TypeSwiftModel.StableFqNameNamespace}${typealiasName}")
//         }
//
//         data class KotlinClass(
//             val containingType: KotlinClass? = null,
//             val kotlinModule: String,
//             val kotlinFqName: FqName,
//             val swiftName: String,
//         ): Local {
//             override val name: String
//                 get() = TODO("Not yet implemented")
//
//             override val typealiasName: String = "class__${kotlinModule}__${kotlinFqName}"
//
//             override val originalSpec: DeclaredTypeName
//                 get() = DeclaredTypeName.qualifiedLocalTypeName(swiftName)
//         }
//
//         data class KotlinFile(
//             val kotlinModule: String,
//             val fileNameFragment: String,
//         ): Local {
//             override val name: String
//                 get() = TODO("Not yet implemented")
//
//             override val typealiasName: String = "file__${kotlinModule}__${fileNameFragment}"
//
//             override val originalSpec: DeclaredTypeName
//                 get() = DeclaredTypeName.qualifiedLocalTypeName(fileNameFragment)
//         }
//
//         data class SwiftType(
//             val swiftName: String,
//             val containingType: NominalType? = null,
//         ): Local {
//             override val name: String
//                 get() = TODO("Not yet implemented")
//
//             override val typealiasName: String = "swift__${listOfNotNull(containingType, swiftName).joinToString("__")}"
//
//             override val originalSpec: DeclaredTypeName
//                 get() = containingType?.toSwiftPoetName()?.nestedType(swiftName) ?: DeclaredTypeName.qualifiedTypeName(swiftName)
//         }
//     }
//
//     data class External(
//         val module: String,
//         override val name: String,
//     ) : SwiftFqName, NominalType {
//         override fun toSwiftPoetName(): DeclaredTypeName = DeclaredTypeName.qualifiedTypeName("$module.$name")
//
//         override fun withTypeArguments(typeArguments: List<SwiftFqName>): Parametrized = Parametrized(this, typeArguments)
//
//         // override fun prefixedWith(prefix: String): NominalType {
//         //     TODO("Not yet implemented")
//         // }
//
//         object Swift {
//             val AnyClass = Swift("AnyClass")
//             val AnyObject = Swift("AnyObject")
//             val AnyHashable = Swift("AnyHashable")
//             val Error = Swift("Error")
//             val UnsafeMutableRawPointer = Swift("UnsafeMutableRawPointer")
//
//             operator fun invoke(name: String) = External("Swift", name)
//         }
//
//         object Foundation {
//             val NSObject = Foundation("NSObject")
//             val Protocol = Foundation("Protocol")
//
//             operator fun invoke(name: String) = External("Foundation", name)
//         }
//     }
//
//     sealed interface Special: SwiftFqName {
//         object Any: Special {
//             override val name: String = "Any"
//
//             override fun toSwiftPoetName(): TypeName = AnyTypeName.INSTANCE
//         }
//
//         object Self: Special {
//             override val name: String = "Self"
//
//             override fun toSwiftPoetName(): TypeName = SelfTypeName.INSTANCE
//         }
//
//         object Void: Special {
//             override val name: String = "Self"
//
//             // Although we use `DeclaredTypeName`, this is not a nominal type but a typealias to an empty tuple.
//             override fun toSwiftPoetName(): TypeName = DeclaredTypeName.qualifiedTypeName("Swift.Void")
//         }
//
//         object KotlinErrorType: Special {
//             override val name: String get() = error("Not available in Swift")
//
//             override fun toSwiftPoetName(): TypeName = error("Not available in Swift")
//         }
//     }
//
//     data class TypeParameter(
//         override val name: String,
//     ): SwiftFqName {
//         override fun toSwiftPoetName(): TypeVariableName = TypeVariableName.typeVariable(
//             name = name,
//         )
//     }
//
//     data class Lambda(
//         val parameterTypes: List<SwiftFqName>,
//         val returnType: SwiftFqName,
//         val isEscaping: Boolean,
//     ): SwiftFqName {
//         override val name: String
//             get() = TODO()
//
//         override fun toSwiftPoetName(): FunctionTypeName = FunctionTypeName.get(
//             parameters = parameterTypes.map {
//                 ParameterSpec.unnamed(it.toSwiftPoetName())
//             },
//             returnType = returnType.toSwiftPoetName(),
//             attributes = if (isEscaping) listOf(AttributeSpec.ESCAPING) else emptyList(),
//         )
//     }
//
//     data class Optional(
//         val wrappedType: SwiftFqName,
//     ): SwiftFqName {
//         override val name: String
//             get() = TODO("Not yet implemented")
//
//
//         override fun toSwiftPoetName(): TypeName = wrappedType.toSwiftPoetName().makeOptional()
//     }
//
//     data class Tuple(
//         val elements: List<SwiftFqName>,
//     ): SwiftFqName {
//         override val name: String
//             get() = TODO("Not yet implemented")
//
//         override fun toSwiftPoetName(): TypeName = TupleTypeName.of(elements.map { "" to it.toSwiftPoetName() })
//     }
// }
//
// typealias SwiftTypeIdentifier = SwiftFqName.NominalType

// interface TypeSwiftModel {
//
//     // TODO: Change to `SwiftIrDeclaration`
//     // val identifier: String
//
//     // val swiftIrDeclaration: SwiftIrExtensibleDeclaration
//
//     // val fqName: SwiftFqName
//
//     /**
//      * Points to the original ObjC class.
//      * This name does not change except when the type itself changes.
//      * For example, it does not change when a new type with the same simple name is added.
//      */
//     // TODO Rename to objCStableFqName
//     // val stableFqName: SwiftFqName
//
//     /**
//      * `stableFqName` for the final Swift class.
//      */
//     // // TODO Rename to fqName and point to bridged.fqName -> fqName to rawFqName
//     // val bridgedOrStableFqName: SwiftFqName
//
//     // val isSwiftSymbol: Boolean
//
//     // TODO Remove and keep only fqName also rename to localFqName
//     // fun fqName(separator: String = DEFAULT_SEPARATOR): SwiftFqName.NominalType
//
//     // TODO Introduce FqName without replace prefix (copy from NestedBridgeTypesApiNotesFix)
//
// }

// val TypeSwiftModel.fqName: SwiftFqName
//     get() = fqName()

/**
 * A -> A
 * A.B.C -> C
 *
 * (Cannot be based on identifier because fqName implementation might differ)
 */
// val TypeSwiftModel.simpleName: String
//     get() = fqName.name.substringAfterLast('.')

/**
 * A -> ε
 * A.B.C -> A.B
 */
// val TypeSwiftModel.packageName: String
//     get() = fqName.name.substringBeforeLast('.', "")

// val TypeSwiftModel.stableSpec: TypeName
//     get() = this.identifier.toSwiftPoetName()

// val KotlinClassSwiftModel.stableSpec: DeclaredTypeName
//     get() = this.identifier.toSwiftPoetName()

// val TypeSwiftModel.bridgedOrStableSpec: TypeName
//     get() = this.bridgedOrStableFqName.spec

// val SwiftFqName.declarationSpecIfNominal: DeclaredTypeName
//     get() = (this as? SwiftFqName.NominalType)?.toSwiftPoetName() ?: error("Not a nominal type: $this")
