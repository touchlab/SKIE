package co.touchlab.skie.phases.oir

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirTypeParameter
import co.touchlab.skie.oir.element.superClass
import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.oir.type.translation.typeParameterScope
import co.touchlab.skie.phases.SirPhase
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.Variance

class CreateOirTypesPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider
    private val kirBuiltins = context.kirBuiltins
    private val oirProvider = context.oirProvider
    private val namer = context.namer
    private val oirTypeTranslator = context.oirTypeTranslator

    context(SirPhase.Context)
    override fun execute() {
        createClasses()

        initializeSuperTypesForAllClasses()

        oirProvider.initializeKotlinClassCache()
    }

    private fun createClasses() {
        kirProvider.allClasses.forEach(::createClass)
    }

    private fun createClass(kirClass: KirClass): OirClass {
        val module = oirProvider.getModule(kirClass.module)

        val oirClass = OirClass(
            name = kirClass.name.objCName,
            parent = oirProvider.getFile(module, kirClass.name.swiftName),
            kind = when (kirClass.kind) {
                KirClass.Kind.Interface -> OirClass.Kind.Protocol
                else -> OirClass.Kind.Class
            },
            origin = OirClass.Origin.Kir(kirClass),
        )

        createTypeParameters(oirClass, kirClass)

        kirClass.oirClass = oirClass

        return oirClass
    }

    private fun createTypeParameters(oirClass: OirClass, kirClass: KirClass) {
        val typeParameters = kirClass.typeParameters.map { it.descriptor }

        createTypeParameters(oirClass, typeParameters, namer)

        kirClass.typeParameters.zip(oirClass.typeParameters).forEach { (kirTypeParameter, oirTypeParameter) ->
            kirTypeParameter.oirTypeParameter = oirTypeParameter
        }
    }

    private fun initializeSuperTypesForAllClasses() {
        kirProvider.allClasses.forEach(::initializeSuperTypes)
    }

    private fun initializeSuperTypes(kirClass: KirClass) {
        val oirClass = kirClass.oirClass
        val oirGenericsScope = kirClass.typeParameterScope

        val superTypesWithoutAny = kirClass.superTypes
            .filter { !KotlinBuiltIns.isAnyOrNullableAny(it.kotlinType) }
            .map { oirTypeTranslator.mapType(it, oirGenericsScope) }
            .filterIsInstance<DeclaredOirType>()

        oirClass.superTypes.addAll(superTypesWithoutAny)

        if (oirClass.kind == OirClass.Kind.Class && oirClass.superClass == null && kirClass != kirBuiltins.Base) {
            oirClass.superTypes.add(kirBuiltins.Base.oirClass.defaultType)
        }
    }

    companion object {

        fun createTypeParameters(oirClass: OirClass, typeParameters: List<TypeParameterDescriptor>, namer: ObjCExportNamer) {
            typeParameters.forEach { typeParameter ->
                OirTypeParameter(
                    name = namer.getTypeParameterName(typeParameter),
                    parent = oirClass,
                    variance = when (typeParameter.variance) {
                        Variance.INVARIANT -> OirTypeParameter.Variance.Invariant
                        Variance.IN_VARIANCE -> OirTypeParameter.Variance.Contravariant
                        Variance.OUT_VARIANCE -> OirTypeParameter.Variance.Covariant
                    },
                    // Bounds are not supported yet.
                )
            }
        }
    }
}
