package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase

// If a parent of a nested class is not exported, then it will have an incorrect FqName.
// This is due to a bug in Kotlin compiler that causes the class to have a nested name even though the class cannot be nested.
// (The nested name is then removed during SirClass instantiation because that uses only the simple name.)
object FixNamesOfInaccessibleNestedClassesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses.forEach(::fixNameOfInaccessibleNestedClass)
    }

    private fun fixNameOfInaccessibleNestedClass(kirClass: KirClass) {
        val swiftName = kirClass.swiftName

        val hasIncorrectName = swiftName.contains(".") && swiftName != kirClass.originalSirClass.fqName.toLocalString()

        if (hasIncorrectName) {
            val containingClassName = swiftName.substringBefore(".")
            val simpleName = swiftName.substringAfter(".")

            kirClass.originalSirClass.baseName = containingClassName + simpleName.replaceFirstChar(Char::uppercaseChar)
        }
    }
}
