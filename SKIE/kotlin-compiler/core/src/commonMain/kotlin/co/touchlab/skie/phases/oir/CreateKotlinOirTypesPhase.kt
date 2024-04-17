package co.touchlab.skie.phases.oir

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirTypeParameter
import co.touchlab.skie.oir.element.toOirKind
import co.touchlab.skie.phases.SirPhase

class CreateKotlinOirTypesPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider
    private val oirProvider = context.oirProvider

    context(SirPhase.Context)
    override suspend fun execute() {
        createClasses()

        oirProvider.initializeKotlinClassCache()
    }

    private fun createClasses() {
        kirProvider.kotlinClasses.forEach(::createClass)
    }

    private fun createClass(kirClass: KirClass): OirClass {
        val module = oirProvider.getKotlinModule(kirClass.module)

        val oirClass = OirClass(
            name = kirClass.objCName,
            parent = oirProvider.getFile(module, kirClass.swiftName),
            kind = kirClass.kind.toOirKind(),
            origin = OirClass.Origin.Kir(kirClass),
        )

        createTypeParameters(kirClass, oirClass)

        kirClass.oirClass = oirClass

        return oirClass
    }

    companion object {

        fun createTypeParameters(kirClass: KirClass, oirClass: OirClass) {
            kirClass.typeParameters.forEach { typeParameter ->
                OirTypeParameter(
                    name = typeParameter.name,
                    parent = oirClass,
                    variance = typeParameter.variance,
                    // Bounds are not supported.
                )
            }

            kirClass.typeParameters.zip(oirClass.typeParameters).forEach { (kirTypeParameter, oirTypeParameter) ->
                kirTypeParameter.oirTypeParameter = oirTypeParameter
            }
        }
    }
}
