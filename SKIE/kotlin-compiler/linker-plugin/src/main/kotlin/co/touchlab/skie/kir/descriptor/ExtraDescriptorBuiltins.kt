package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName

class ExtraDescriptorBuiltins(
    private val exposedModules: Set<ModuleDescriptor>,
) {

    private val Foundation = getModule("<org.jetbrains.kotlin.native.platform.Foundation>")

    private val Darwin = getModule("<org.jetbrains.kotlin.native.platform.darwin>")

    val NSObject: ClassDescriptor = getClass("platform.darwin.NSObject", Darwin)

    val NSCopying: ClassDescriptor = getClass("platform.Foundation.NSCopyingProtocol", Foundation)

    val NSError: ClassDescriptor = getClass("platform.Foundation.NSError", Foundation)

    val NSString: ClassDescriptor = getClass("platform.Foundation.NSString", Foundation)

    val NSArray: ClassDescriptor = getClass("platform.Foundation.NSArray", Foundation)

    val NSMutableArray: ClassDescriptor = getClass("platform.Foundation.NSMutableArray", Foundation)

    val NSSet: ClassDescriptor = getClass("platform.Foundation.NSSet", Foundation)

    val NSMutableSet: ClassDescriptor = getClass("platform.Foundation.NSMutableSet", Foundation)

    val NSDictionary: ClassDescriptor = getClass("platform.Foundation.NSDictionary", Foundation)

    val NSMutableDictionary: ClassDescriptor = getClass("platform.Foundation.NSMutableDictionary", Foundation)

    val NSNumber: ClassDescriptor = getClass("platform.Foundation.NSNumber", Foundation)

    private fun getModule(name: String): ModuleDescriptor =
        exposedModules.flatMap { it.allDependencyModules }.first { it.name.asString() == name }

    private fun getClass(name: String, module: ModuleDescriptor): ClassDescriptor =
        module.resolveClassByFqName(FqName(name), NoLookupLocation.FROM_BACKEND)
            ?: error("Class $name not found in module ${module.name}")
}
