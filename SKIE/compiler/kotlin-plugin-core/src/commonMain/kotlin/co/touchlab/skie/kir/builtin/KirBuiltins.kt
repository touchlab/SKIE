package co.touchlab.skie.kir.builtin

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirModule

@Suppress("PropertyName")
interface KirBuiltins {

    val stdlibModule: KirModule

    val builtinClasses: Set<KirClass>

    val NSObject: KirClass

    val NSCopying: KirClass

    val NSError: KirClass

    val NSString: KirClass

    val NSArray: KirClass

    val NSMutableArray: KirClass

    val NSSet: KirClass

    val NSMutableSet: KirClass

    val NSDictionary: KirClass

    val NSMutableDictionary: KirClass

    val NSNumber: KirClass

    val Base: KirClass

    val MutableSet: KirClass

    val MutableMap: KirClass

    val Number: KirClass

    val nsNumberDeclarationsByFqName: Map<String, KirClass>
}
