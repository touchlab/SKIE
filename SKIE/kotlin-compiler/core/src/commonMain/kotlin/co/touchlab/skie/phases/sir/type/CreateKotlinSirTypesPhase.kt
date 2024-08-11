package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.configuration.SkieVisibility
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.toConformanceBound
import co.touchlab.skie.sir.element.toSirKind
import co.touchlab.skie.util.toSirVisibility

// Must be class because it has state
class CreateKotlinSirTypesPhase : SirPhase {

    private val kirToSirClasses = mutableMapOf<KirClass, SirClass>()

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses.forEach {
            getOrCreateClass(it)
        }
    }

    context(SirPhase.Context)
    private fun getOrCreateClass(kirClass: KirClass): SirClass =
        kirToSirClasses.getOrPut(kirClass) {
            createClass(kirClass)
        }

    context(SirPhase.Context)
    private fun createClass(kirClass: KirClass): SirClass {
        val sirClass = SirClass(
            baseName = kirClass.sirFqName.simpleName,
            parent = kirClass.sirParent,
            kind = kirClass.oirClass.kind.toSirKind(),
            origin = SirClass.Origin.Kir(kirClass),
            visibility = kirClass.configuration[SkieVisibility].toSirVisibility(),
            isReplaced = kirClass.configuration[SkieVisibility] in listOf(SkieVisibility.Level.PublicButReplaced, SkieVisibility.Level.InternalAndReplaced),
            isHidden = kirClass.configuration[SkieVisibility] in listOf(SkieVisibility.Level.PublicButHidden, SkieVisibility.Level.PublicButReplaced),
        )

        createTypeParameters(kirClass.oirClass, sirClass)

        kirClass.oirClass.originalSirClass = sirClass

        return sirClass
    }

    context(SirPhase.Context)
    private val KirClass.sirFqName: SirFqName
        get() {
            val firstComponent = this.swiftName.substringBefore(".")
            val secondComponent = this.swiftName.substringAfter(".").takeIf { it.isNotBlank() }

            val firstName = SirFqName(
                module = sirProvider.kotlinModule,
                simpleName = firstComponent,
            )

            return if (secondComponent != null) firstName.nested(secondComponent) else firstName
        }

    context(SirPhase.Context)
    private val KirClass.sirParent: SirDeclarationParent
        get() = sirFqName.parent?.simpleName?.let { findSirParentRecursively(this, it) } ?: sirProvider.kotlinModule.builtInFile

    context(SirPhase.Context)
    private fun findSirParentRecursively(kirClass: KirClass, parentName: String): SirClass? =
        when (val parent = kirClass.parent) {
            is KirClass -> if (parent.swiftName == parentName) getOrCreateClass(parent) else findSirParentRecursively(parent, parentName)
            is KirModule -> null
        }

    companion object {

        context(SirPhase.Context)
        fun createTypeParameters(oirClass: OirClass, sirClass: SirClass) {
            oirClass.typeParameters.forEach { typeParameter ->
                typeParameter.sirTypeParameter = SirTypeParameter(
                    name = typeParameter.name,
                    parent = sirClass,
                    bounds = listOf(sirBuiltins.Swift.AnyObject.defaultType.toConformanceBound()),
                )
            }
        }
    }
}
