package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.coerceAtMostInSwift
import co.touchlab.skie.sir.element.kirClassOrNull
import co.touchlab.skie.sir.element.superClassType
import co.touchlab.skie.sir.element.superProtocolTypes
import co.touchlab.skie.sir.type.SirType

object PropagateSirVisibilityToClassesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val updaterProvider = TypeVisibilityUpdaterProvider(sirProvider.allLocalClasses)

        updaterProvider.allTypeVisibilityUpdaters.forEach {
            it.propagateVisibility()
        }
    }

    private class TypeVisibilityUpdaterProvider(
        sirClasses: List<SirClass>,
    ) {

        private val cache = sirClasses.associateWith { ClassVisibilityUpdater(it) }

        init {
            cache.values.forEach {
                it.initialize(this)
            }
        }

        val allTypeVisibilityUpdaters: Collection<ClassVisibilityUpdater> = cache.values

        operator fun get(sirClass: SirClass): ClassVisibilityUpdater =
            cache.getValue(sirClass)
    }

    private class ClassVisibilityUpdater(private val sirClass: SirClass) {

        private val directDependents = mutableListOf<ClassVisibilityUpdater>()

        fun initialize(typeVisibilityUpdaterProvider: TypeVisibilityUpdaterProvider) {
            getDependencies()
                .filter { it.module is SirModule.Skie || it.module is SirModule.Kotlin }
                .forEach {
                    typeVisibilityUpdaterProvider[it].registerDirectDependentDeclaration(this)
                }
        }

        private fun registerDirectDependentDeclaration(typeVisibilityUpdater: ClassVisibilityUpdater) {
            directDependents.add(typeVisibilityUpdater)
        }

        fun propagateVisibility() {
            directDependents.forEach {
                it.coerceVisibilityAtMost(sirClass.visibility)
            }
        }

        private fun coerceVisibilityAtMost(limit: SirVisibility) {
            val newVisibility = sirClass.visibility.coerceAtMostInSwift(limit)

            if (sirClass.visibility == newVisibility) {
                return
            }

            sirClass.visibility = newVisibility

            propagateVisibility()
        }

        private fun getDependencies(): Set<SirClass> =
            setOfNotNull(
                sirClass.namespace?.classDeclaration,
                (sirClass.kirClassOrNull?.parent as? KirClass)?.originalSirClass,
            ) +
                sirClass.typeParameters.flatMap { typeParameter -> typeParameter.bounds.flatMap { it.referencedClasses } }.toSet() +
                getSuperTypesDependencies()

        private fun getSuperTypesDependencies(): List<SirClass> =
            if (sirClass.kind != SirClass.Kind.Protocol) {
                val classSuperType = listOfNotNull(sirClass.superClassType)
                val protocolSuperTypes = sirClass.superProtocolTypes

                val consideredTypes = protocolSuperTypes.flatMap { it.typeArguments } + classSuperType

                consideredTypes.flatMap { it.referencedClasses }
            } else {
                sirClass.superTypes.flatMap { it.referencedClasses }
            }

        private val SirType.referencedClasses: List<SirClass>
            get() = this.normalizedEvaluatedType().referencedTypeDeclarations
                .map { it as? SirClass ?: error("Normalized type should only reference SirClasses: $it") }
    }
}
