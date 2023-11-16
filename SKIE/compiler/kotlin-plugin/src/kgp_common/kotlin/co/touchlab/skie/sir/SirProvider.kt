package co.touchlab.skie.sir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTopLevelDeclarationParent
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.getAllDeclarationsRecursively
import co.touchlab.skie.sir.element.isAccessibleFromOtherModules
import co.touchlab.skie.util.FrameworkLayout
import java.nio.file.Path

class SirProvider(
    framework: FrameworkLayout,
    private val kirProvider: KirProvider,
) {

    val kotlinModule: SirModule.Kotlin = SirModule.Kotlin(framework.moduleName)

    val skieModule: SirModule.Skie = SirModule.Skie(framework.moduleName)

    val sirBuiltins by lazy {
        SirBuiltins(this)
    }

    private val externalModuleCache = mutableMapOf<String, SirModule.External>()

    private val fileByPathCache = mutableMapOf<Path, SirFile>()

    private val extensionCache = mutableMapOf<Pair<SirClass, SirTopLevelDeclarationParent>, SirExtension>()

    val files: Collection<SirFile>
        get() = skieModule.files

    val allLocalDeclarations: List<SirDeclaration>
        get() = listOf(skieModule, kotlinModule).flatMap { it.getAllDeclarationsRecursively() }

    val allLocalTypeDeclarations: List<SirTypeDeclaration>
        get() = allLocalDeclarations.filterIsInstance<SirTypeDeclaration>()

    val allLocalClasses: List<SirClass>
        get() = allLocalTypeDeclarations.filterIsInstance<SirClass>()

    val allLocalEnums: List<SirClass>
        get() = allLocalClasses.filter { it.kind == SirClass.Kind.Enum }

    val allExternalTypeDeclarations: List<SirTypeDeclaration>
        get() = externalModuleCache.values
            .flatMap { it.getAllDeclarationsRecursively() }
            .filterIsInstance<SirTypeDeclaration>()

    val allSkieGeneratedDeclarations: List<SirDeclaration>
        get() = skieModule.getAllDeclarationsRecursively()

    val allSkieGeneratedTopLevelDeclarations: List<SirDeclaration>
        get() = skieModule.declarations + files.flatMap { it.declarations }

    val allSkieGeneratedCallableDeclarations: List<SirCallableDeclaration>
        get() = allSkieGeneratedDeclarations.filterIsInstance<SirCallableDeclaration>()

    val allSkieGeneratedSimpleFunctions: List<SirSimpleFunction>
        get() = allSkieGeneratedCallableDeclarations.filterIsInstance<SirSimpleFunction>()

    fun getSkieNamespaceFile(name: String): SirFile =
        getFile(kirProvider.skieModule.name, name)

    fun getFile(namespace: String, name: String): SirFile =
        fileByPathCache.getOrPut(SirFile.relativePath(namespace, name)) {
            SirFile(namespace, name, skieModule)
        }

    // Do not use for Extensions with ConditionalConstraints as that would break caching.
    fun getExtension(
        classDeclaration: SirClass,
        parent: SirTopLevelDeclarationParent,
        visibility: SirVisibility = SirVisibility.Public,
    ): SirExtension =
        extensionCache.getOrPut(classDeclaration to parent) {
            SirExtension(classDeclaration, parent, visibility)
        }

    fun getExternalModule(moduleName: String): SirModule.External =
        externalModuleCache.getOrPut(moduleName) {
            SirModule.External(moduleName)
        }

    fun findClassByFqName(fqName: SirFqName): SirClass? {
        val parent = fqName.parent?.let { getClassByFqName(it) }

        val possibleParentDeclarations = if (parent != null) {
            listOf(parent)
        } else {
            fqName.module.files + fqName.module
        }

        possibleParentDeclarations.forEach { possibleParent ->
            possibleParent.declarations.forEach { possibleDeclaration ->
                if (possibleDeclaration is SirClass && possibleDeclaration.fqName == fqName) {
                    return possibleDeclaration
                }
            }
        }

        return null
    }

    fun getClassByFqName(fqName: SirFqName): SirClass =
        findClassByFqName(fqName)
            ?: error("SirClass with fqName $fqName not found.")
}

context(SirProvider)
fun SirTopLevelDeclarationParent.getExtension(
    classDeclaration: SirClass,
    visibility: SirVisibility = SirVisibility.Public,
): SirExtension =
    getExtension(classDeclaration, this, visibility)

context(SirPhase.Context)
fun SirTopLevelDeclarationParent.getExtension(
    classDeclaration: SirClass,
    visibility: SirVisibility = SirVisibility.Public,
): SirExtension =
    sirProvider.getExtension(classDeclaration, this, visibility)

