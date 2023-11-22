package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.toSirKind

class CreateKotlinSirTypesPhase(
    val context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider
    private val sirProvider = context.sirProvider
    private val sirBuiltins = context.sirBuiltins

    context(SirPhase.Context)
    override fun execute() {
        createClasses()

        kirProvider.initializeSirClassCache()
    }

    private val kirToSirClasses = mutableMapOf<KirClass, SirClass>()

    private fun createClasses() {
        kirProvider.allClasses.forEach(::getOrCreateClass)
    }

    private fun getOrCreateClass(kirClass: KirClass): SirClass =
        kirToSirClasses.getOrPut(kirClass) {
            createClass(kirClass)
        }

    private fun createClass(kirClass: KirClass): SirClass {
        val sirClass = SirClass(
            baseName = kirClass.sirFqName.simpleName,
            parent = kirClass.sirParent,
            kind = kirClass.oirClass.kind.toSirKind(),
            origin = SirClass.Origin.Kir(kirClass),
        )

        sirClass.addTypeParameters(kirClass)

        kirClass.oirClass.originalSirClass = sirClass

        return sirClass
    }

    private fun SirClass.addTypeParameters(kirClass: KirClass) {
        kirClass.typeParameters.forEach { typeParameter ->
            typeParameter.oirTypeParameter.sirTypeParameter = SirTypeParameter(
                name = typeParameter.oirTypeParameter.name,
                bounds = listOf(sirBuiltins.Swift.AnyObject.defaultType),
            )
        }
    }

    private val KirClass.sirFqName: SirFqName
        get() {
            val swiftName = name.swiftName

            val firstComponent = swiftName.substringBefore(".")
            val secondComponent = swiftName.substringAfter(".").takeIf { it.isNotBlank() }

            val firstName = SirFqName(
                module = sirProvider.kotlinModule,
                simpleName = firstComponent,
            )

            return if (secondComponent != null) firstName.nested(secondComponent) else firstName
        }

    private val KirClass.sirParent: SirDeclarationParent
        get() = sirFqName.parent?.simpleName?.let { findSirParentRecursively(this, it) } ?: sirProvider.kotlinModule.module

    private fun findSirParentRecursively(kirClass: KirClass, parentName: String): SirClass? =
        when (val parent = kirClass.parent) {
            is KirClass -> if (parent.name.swiftName == parentName) getOrCreateClass(parent) else findSirParentRecursively(parent, parentName)
            is KirModule -> null
        }
}
