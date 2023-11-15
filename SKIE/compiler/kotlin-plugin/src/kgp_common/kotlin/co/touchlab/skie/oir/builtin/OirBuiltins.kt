package co.touchlab.skie.oir.builtin

import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.oir.OirProvider
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirModule
import co.touchlab.skie.oir.element.OirTypeParameter
import co.touchlab.skie.oir.element.toTypeParameterUsage

@Suppress("PropertyName")
class OirBuiltins(
    oirProvider: OirProvider,
    extraDescriptorBuiltins: ExtraDescriptorBuiltins,
) {

    private val Foundation: OirModule.External = oirProvider.getExternalModule("Foundation")

    val NSObject: OirClass = oirProvider.getExternalClass(extraDescriptorBuiltins.NSObject, Foundation)

    val NSError: OirClass = oirProvider.getExternalClass(extraDescriptorBuiltins.NSError).apply {
        superTypes.add(NSObject.defaultType)
    }

    val NSString: OirClass = oirProvider.getExternalClass(extraDescriptorBuiltins.NSString).apply {
        superTypes.add(NSObject.defaultType)
    }

    val NSArray: OirClass = oirProvider.getExternalClass(extraDescriptorBuiltins.NSArray).apply {
        superTypes.add(NSObject.defaultType)

        OirTypeParameter(
            name = "E",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )
    }

    val NSMutableArray: OirClass = oirProvider.getExternalClass(extraDescriptorBuiltins.NSMutableArray).apply {
        val typeParameter = OirTypeParameter(
            name = "E",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val superType = NSArray.toType(typeParameter.toTypeParameterUsage())

        superTypes.add(superType)
    }

    val NSSet: OirClass = oirProvider.getExternalClass(extraDescriptorBuiltins.NSSet).apply {
        superTypes.add(NSObject.defaultType)

        OirTypeParameter(
            name = "E",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )
    }

    val NSDictionary: OirClass = oirProvider.getExternalClass(extraDescriptorBuiltins.NSDictionary).apply {
        superTypes.add(NSObject.defaultType)

        OirTypeParameter(
            name = "K",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        OirTypeParameter(
            name = "V",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )
    }
}
