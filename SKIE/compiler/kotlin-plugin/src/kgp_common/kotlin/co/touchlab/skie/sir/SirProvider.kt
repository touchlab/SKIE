package co.touchlab.skie.sir

import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.ConfigurationProvider
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.cinteropClassDescriptorOrNull
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTopLevelDeclarationParent
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.getAllDeclarationsRecursively
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.directory.SkieBuildDirectory

class SirProvider(
    framework: FrameworkLayout,
    kirProvider: KirProvider,
    private val configurationProvider: ConfigurationProvider,
    skieConfiguration: SkieConfiguration,
    skieBuildDirectory: SkieBuildDirectory,
) {

    val kotlinModule: SirModule.Kotlin = SirModule.Kotlin(framework.moduleName)

    val skieModule: SirModule.Skie = SirModule.Skie(framework.moduleName)

    val fileProvider: SirFileProvider = SirFileProvider(skieModule, kirProvider, skieBuildDirectory)

    val sirBuiltins by lazy {
        SirBuiltins(this, skieConfiguration)
    }

    private val externalModuleCache = mutableMapOf<String, SirModule.External>()

    private val extensionCache = mutableMapOf<Pair<SirClass, SirTopLevelDeclarationParent>, SirExtension>()

    val skieModuleFiles: Collection<SirFile>
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
        get() = skieModuleFiles.filterIsInstance<SirDeclarationParent>().flatMap { it.declarations }

    val allSkieGeneratedCallableDeclarations: List<SirCallableDeclaration>
        get() = allSkieGeneratedDeclarations.filterIsInstance<SirCallableDeclaration>()

    val allSkieGeneratedSimpleFunctions: List<SirSimpleFunction>
        get() = allSkieGeneratedCallableDeclarations.filterIsInstance<SirSimpleFunction>()

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
        findClassByFqName(fqName)
            ?: error("SirClass with fqName $fqName not found.")

    fun findExternalModule(oirClass: OirClass): SirModule.External? {
        val classDescriptor = oirClass.cinteropClassDescriptorOrNull ?: error("Invalid origin for OirClass: $oirClass")

        val moduleName = configurationProvider.getConfiguration(classDescriptor)[ClassInterop.CInteropFrameworkName] ?: return null

        return getExternalModule(moduleName)
    }
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

