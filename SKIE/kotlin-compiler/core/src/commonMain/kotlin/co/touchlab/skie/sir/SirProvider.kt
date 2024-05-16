package co.touchlab.skie.sir

import co.touchlab.skie.configuration.RootConfiguration
import co.touchlab.skie.kir.element.KirModule.Origin
import co.touchlab.skie.oir.OirProvider
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.kirClassOrNull
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirCompilableFile
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTopLevelDeclarationParent
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.getAllDeclarationsRecursively
import co.touchlab.skie.util.directory.FrameworkLayout
import co.touchlab.skie.util.directory.SkieBuildDirectory

class SirProvider(
    framework: FrameworkLayout,
    private val oirProvider: OirProvider,
    skieBuildDirectory: SkieBuildDirectory,
    rootConfiguration: RootConfiguration,
) {

    val kotlinModule: SirModule.Kotlin = SirModule.Kotlin(framework.frameworkName)

    val skieModule: SirModule.Skie = SirModule.Skie(framework.frameworkName)

    private val unknownModule: SirModule.Unknown = SirModule.Unknown()

    val fileProvider: SirFileProvider = SirFileProvider(skieModule, skieBuildDirectory)

    val sirBuiltins by lazy {
        SirBuiltins(this, rootConfiguration)
    }

    private val externalModuleCache = mutableMapOf<String, SirModule.External>()

    private val extensionCache = mutableMapOf<Pair<SirClass, SirTopLevelDeclarationParent>, SirExtension>()

    val skieModuleFiles: Collection<SirFile>
        get() = skieModule.files

    val compilableFiles: List<SirCompilableFile>
        get() = skieModuleFiles.filterIsInstance<SirCompilableFile>()

    val allLocalDeclarations: List<SirDeclaration>
        get() = listOf(skieModule, kotlinModule).flatMap { it.getAllDeclarationsRecursively() }

    val allLocalTypeDeclarations: List<SirTypeDeclaration>
        get() = allLocalDeclarations.filterIsInstance<SirTypeDeclaration>()

    val allLocalClasses: List<SirClass>
        get() = allLocalTypeDeclarations.filterIsInstance<SirClass>()

    val allLocalTypeAliases: List<SirTypeAlias>
        get() = allLocalTypeDeclarations.filterIsInstance<SirTypeAlias>()

    val allLocalEnums: List<SirClass>
        get() = allLocalClasses.filter { it.kind == SirClass.Kind.Enum }

    val allUsedExternalModules: List<SirModule.External>
        get() = oirProvider.externalClassesAndProtocols
            .map { it.originalSirClass.module }
            .filterIsInstance<SirModule.External>()
            .distinct()

    val allExternalTypeDeclarations: List<SirTypeDeclaration>
        get() = externalModuleCache.values
            .flatMap { it.getAllDeclarationsRecursively() }
            .filterIsInstance<SirTypeDeclaration>()

    val allSkieGeneratedDeclarations: List<SirDeclaration>
        get() = skieModule.getAllDeclarationsRecursively()

    val allSkieGeneratedTopLevelDeclarations: List<SirDeclaration>
        get() = skieModuleFiles.filterIsInstance<SirDeclarationParent>().flatMap { it.declarations }

    val allSkieGeneratedCallableDeclarations: List<SirCallableDeclaration>
        get() = allSkieGeneratedDeclarations.filterIsInstance<SirCallableDeclaration>()

    val allSkieGeneratedSimpleFunctions: List<SirSimpleFunction>
        get() = allSkieGeneratedCallableDeclarations.filterIsInstance<SirSimpleFunction>()

    // Do not use for Extensions with ConditionalConstraints as that would break caching.
    fun getExtension(
        classDeclaration: SirClass,
        parent: SirTopLevelDeclarationParent,
    ): SirExtension =
        extensionCache.getOrPut(classDeclaration to parent) {
            SirExtension(classDeclaration, parent)
        }

    fun getExternalModule(moduleName: String): SirModule.External =
        externalModuleCache.getOrPut(moduleName) {
            SirModule.External(moduleName)
        }

    fun findClassByFqName(fqName: SirFqName): SirClass? {
        val parent = fqName.parent?.let { findClassByFqName(it) ?: return null }

        val possibleParentDeclarations = if (parent != null) {
            listOf(parent)
        } else {
            fqName.module.files.filterIsInstance<SirDeclarationParent>()
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
        findClassByFqName(fqName) ?: error("SirClass with fqName $fqName not found.")

    fun findModuleForKnownExternalClass(oirClass: OirClass): SirModule? {
        val kirModule = oirClass.kirClassOrNull?.module

        return if (kirModule?.origin == Origin.KnownExternal) {
            getExternalModule(kirModule.name)
        } else {
            null
        }
    }

    fun getModuleForExternalClass(oirClass: OirClass): SirModule =
        findModuleForKnownExternalClass(oirClass) ?: unknownModule
}

context(SirProvider)
fun SirTopLevelDeclarationParent.getExtension(
    classDeclaration: SirClass,
): SirExtension =
    getExtension(classDeclaration, this)

context(SirPhase.Context)
fun SirTopLevelDeclarationParent.getExtension(
    classDeclaration: SirClass,
): SirExtension =
    sirProvider.getExtension(classDeclaration, this)

