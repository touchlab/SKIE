package co.touchlab.skie.plugin.api.sir.declaration

import co.touchlab.skie.plugin.api.util.nsNumberKindClassIds
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.name.FqName

object BuiltinDeclarations {

    object Any: SwiftIrDeclaration {

    }

    // TODO: This should be a typealias declaration to ()
    object Void: SwiftIrDeclaration {

    }

    // TODO: This should be a typealias declaration to AnyObject.Type
    object AnyClass: SwiftIrDeclaration {

    }

    object Protocol: SwiftIrDeclaration {

    }

    object Swift {
        val module = SwiftIrModule("Swift")

        val Hashable = "Hashable"()
        val AnyHashable = "AnyHashable"()

        // TODO: Check if AnyObject isn't a special type
        val AnyObject = "AnyObject"()
        val UnsafeMutableRawPointer = "UnsafeMutableRawPointer"()

        val Error = "Error"()

        // MARK:- Primitive Types
        val Int = "Int"()
        val UInt = "UInt"()
        val Bool = "Bool"()
        val Int8 = "Int8"()
        val UInt8 = "UInt8"()
        val Int16 = "Int16"()
        val UInt16 = "UInt16"()
        val Int32 = "Int32"()
        val UInt32 = "UInt32"()
        val Int64 = "Int64"()
        val UInt64 = "UInt64"()
        val Float = "Float"()
        val Double = "Double"()

        val String = "String"()
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
        )

        private operator fun String.invoke(
            typeParameters: List<SwiftIrTypeParameterDeclaration> = emptyList(),
        ): SwiftIrTypeDeclaration.External {
            return SwiftIrTypeDeclaration.External(
                module = module,
                name = this,
            )
        }
    }

    object Foundation {

        val module = SwiftIrModule("Foundation")

        val NSObject = "NSObject"()
        val unichar = "unichar"()
        val NSString = "NSString"()
        val NSArray = "NSArray"()
        val NSMutableArray = "NSMutableArray"()
        val NSDictionary = "NSDictionary"()
        val NSSet = "NSSet"()

        private operator fun String.invoke(): SwiftIrTypeDeclaration.External {
            return SwiftIrTypeDeclaration.External(
                module = module,
                name = this,
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
