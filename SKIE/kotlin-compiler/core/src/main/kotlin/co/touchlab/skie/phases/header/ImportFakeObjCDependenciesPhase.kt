package co.touchlab.skie.phases.header

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirModule

/**
 * A workaround for what's likely a bug in the Swift compiler.
 * For an unknown reason, forward declarations are not enough for the Swift compiler to correctly infer Obj-C types as Hashable in situation like:
 *
 * ```
 * sealed class X<T>
 *
 * sealed class A : X<ExternalType>
 *
 * class B : A()
 * ```
 *
 * As a result, the generated Sealed class A cannot conform to Hashable because the class B is not considered Hashable by the Swift compiler.
 * (Even though the problematic type argument is erased at runtime.)
 *
 * The workaround is to add fake definitions of all external types into the Kotlin Obj-C header file but just for the Swift compilation.
 *
 * An alternative solution is to import the Framework in the Swift code with the generated sealed class.
 * However, that approach would require detecting the issue upfront (or include all frameworks in all swift files).
 */
object ImportFakeObjCDependenciesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        // TODO: Make sure this runs for both 'fake' and SDK modules once support for distinction between the two module types is added.
        val fakeExternalModules = sirProvider.allUsedExternalModules.filter { it.name != "Foundation" }

        if (fakeExternalModules.isEmpty()) {
            return
        }

        val originalContent = framework.kotlinHeader.readText()

        importFakeFrameworks(fakeExternalModules, originalContent)

        revertHeaderChange(originalContent)
    }

    context(SirPhase.Context)
    private fun importFakeFrameworks(fakeExternalModules: List<SirModule.External>, originalHeader: String) {
        val fakeImports = fakeExternalModules.joinToString("\n") { module ->
            // TODO: Properly fix this for nested modules as part of work to add distinction between the fake and SDK modules.
            knownNestedSdkModules[module.name]?.let { parentName ->
                "@import $parentName.${module.name};"
            } ?: "@import ${module.name};"
        }

        val updatedContent = originalHeader + "\n$fakeImports"

        framework.kotlinHeader.writeText(updatedContent)
    }

    context(SirPhase.Context)
    private fun revertHeaderChange(originalHeader: String) {
        doInPhase(RevertPhase) {
            framework.kotlinHeader.writeText(originalHeader)
        }
    }

    object RevertPhase : StatefulSirPhase()
}
