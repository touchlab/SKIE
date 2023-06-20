package co.touchlab.skie.plugin.api.sir.declaration

import co.touchlab.skie.plugin.api.util.nsNumberKindClassIds
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.name.FqName

object BuiltinDeclarations {

    object Any: SwiftIrDeclaration {
        override fun toString(): String = "Any"
    }

    // TODO: This should be a typealias declaration to ()
    object Void: SwiftIrDeclaration {
        override fun toString(): String = "Swift.Void"
    }

    // TODO: This should be a typealias declaration to AnyObject.Type
    object AnyClass: SwiftIrDeclaration {
        override fun toString(): String = "Swift.AnyClass"
    }

    object Protocol: SwiftIrDeclaration {
        override fun toString(): String = "Protocol"
    }

    object Swift {
        val module = SwiftIrModule("Swift")

        val Hashable = "Hashable"()
        val AnyHashable = "AnyHashable"(superTypes = listOf(Hashable))

        // TODO: Check if AnyObject isn't a special type
        val AnyObject = "AnyObject"()
        val UnsafeMutableRawPointer = "UnsafeMutableRawPointer"()

        val Error = "Error"()

        // MARK:- Primitive Types
        val Int = "Int"(superTypes = listOf(Hashable))
        val UInt = "UInt"(superTypes = listOf(Hashable))
        val Bool = "Bool"(superTypes = listOf(Hashable))
        val Int8 = "Int8"(superTypes = listOf(Hashable))
        val UInt8 = "UInt8"(superTypes = listOf(Hashable))
        val Int16 = "Int16"(superTypes = listOf(Hashable))
        val UInt16 = "UInt16"(superTypes = listOf(Hashable))
        val Int32 = "Int32"(superTypes = listOf(Hashable))
        val UInt32 = "UInt32"(superTypes = listOf(Hashable))
        val Int64 = "Int64"(superTypes = listOf(Hashable))
        val UInt64 = "UInt64"(superTypes = listOf(Hashable))
        val Float = "Float"(superTypes = listOf(Hashable))
        val Double = "Double"(superTypes = listOf(Hashable))

        val String = "String"(
            superTypes = listOf(Hashable),
        )
        val Array = "Array"(
            typeParameters = listOf(
                SwiftIrTypeParameterDeclaration.SwiftTypeParameter(
                    name = "Element",
                    bounds = emptyList(),
                )
            ),
        )
        val Dictionary = "Dictionary"(
            typeParameters = listOf(
                SwiftIrTypeParameterDeclaration.SwiftTypeParameter(
                    name = "Key",
                    bounds = listOf(Hashable),
                ),
                SwiftIrTypeParameterDeclaration.SwiftTypeParameter(
                    name = "Value",
                    bounds = emptyList(),
                ),
            ),
        )
        val Set = "Set"(
            typeParameters = listOf(
                SwiftIrTypeParameterDeclaration.SwiftTypeParameter(
                    name = "Element",
                    bounds = listOf(Hashable),
                )
            ),
            superTypes = listOf(Hashable),
        )

        private operator fun String.invoke(
            typeParameters: List<SwiftIrTypeParameterDeclaration> = emptyList(),
            superTypes: List<SwiftIrTypeDeclaration> = emptyList(),
        ): SwiftIrTypeDeclaration.External {
            return SwiftIrTypeDeclaration.External(
                module = module,
                name = this,
                typeParameters = typeParameters,
                superTypes = superTypes,
            )
        }
    }

    object Foundation {

        val module = SwiftIrModule("Foundation")

        val NSObject = "NSObject"(superTypes = listOf(Swift.Hashable, Swift.AnyObject))
        val unichar = "unichar"()
        val NSString = "NSString"()
        val NSArray = "NSArray"()
        val NSMutableArray = "NSMutableArray"()
        val NSDictionary = "NSDictionary"()
        val NSSet = "NSSet"()

        private operator fun String.invoke(
            superTypes: List<SwiftIrTypeDeclaration> = emptyList(),
        ): SwiftIrTypeDeclaration.External {
            return SwiftIrTypeDeclaration.External(
                module = module,
                name = this,
                superTypes = superTypes,
            )
        }
    }

    class Kotlin(
        val namer: ObjCExportNamer,
    ) {
        val allDeclarations: List<SwiftIrTypeDeclaration.Local.ObjcType> by lazy {
            listOf(
                Base,
                MutableSet,
                MutableMap,
                Number,
            ) + nsNumberDeclarations.values
        }

        // TODO: Delete these (along with the `SwiftIrTypeDeclaration.Local.ObjcType` declaration) once we generate typealiases for these classes:
        val Base by lazy {
            SwiftIrTypeDeclaration.Local.ObjcType(
                swiftName = namer.kotlinAnyName.swiftName,
                superTypes = listOf(Foundation.NSObject),
            )
        }
        val MutableSet by lazy {
            SwiftIrTypeDeclaration.Local.ObjcType(
                swiftName = namer.mutableSetName.swiftName,
                typeParameters = listOf(
                    SwiftIrTypeParameterDeclaration.SwiftTypeParameter(
                        name = "E",
                        bounds = listOf(Swift.Hashable),
                    )
                ),
                superTypes = listOf(Base),
            )
        }
        val MutableMap by lazy {
            SwiftIrTypeDeclaration.Local.ObjcType(
                swiftName = namer.mutableMapName.swiftName,
                superTypes = listOf(Base),
            )
        }

        val Number by lazy {
            SwiftIrTypeDeclaration.Local.ObjcType(
                swiftName = namer.kotlinNumberName.swiftName,
                // FIXME: Is it base, or NSNumber?
                superTypes = listOf(Base),
            )
        }

        val nsNumberDeclarations by lazy {
            nsNumberKindClassIds().associateWith {
                SwiftIrTypeDeclaration.Local.ObjcType(
                    swiftName = namer.numberBoxName(it).swiftName,
                    // FIXME: Is it base, or NSNumber?
                    superTypes = listOf(Base),
                )
            }
        }

        // TODO: Uncomment once we generate typealiases for these classes:
        // val Base by lazy {
        //     SwiftIrTypeDeclaration.Local.KotlinClass.Immutable(
        //         kotlinModule = "kotlin",
        //         kotlinFqName = FqName("kotlin.Any"),
        //         swiftName = namer.kotlinAnyName.swiftName,
        //         superTypes = listOf(Foundation.NSObject),
        //     )
        // }
        // val MutableSet by lazy {
        //     SwiftIrTypeDeclaration.Local.KotlinClass.Immutable(
        //         kotlinModule = "kotlin.collections",
        //         kotlinFqName = FqName("kotlin.collections.MutableSet"),
        //         swiftName = namer.mutableSetName.swiftName,
        //         typeParameters = listOf(
        //             SwiftIrTypeParameterDeclaration.SwiftTypeParameter(
        //                 name = "E",
        //                 bounds = listOf(Swift.Hashable),
        //             )
        //         ),
        //         superTypes = listOf(Base),
        //     )
        // }
        // val MutableMap by lazy {
        //     SwiftIrTypeDeclaration.Local.KotlinClass.Immutable(
        //         kotlinModule = "kotlin.collections",
        //         kotlinFqName = FqName("kotlin.collections.MutableMap"),
        //         swiftName = namer.mutableMapName.swiftName,
        //         superTypes = listOf(Base),
        //     )
        // }
        //
        // val nsNumberDeclarations by lazy {
        //     nsNumberKindClassIds().associateWith {
        //         SwiftIrTypeDeclaration.Local.KotlinClass.Immutable(
        //             kotlinModule = "kotlin",
        //             kotlinFqName = it.asSingleFqName(),
        //             swiftName = namer.numberBoxName(it).swiftName,
        //             // FIXME: Is it base, or NSNumber?
        //             superTypes = listOf(Base),
        //         )
        //     }
        // }
    }
}
