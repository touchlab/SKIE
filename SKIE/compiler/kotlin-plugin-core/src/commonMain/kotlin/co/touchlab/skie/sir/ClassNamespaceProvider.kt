package co.touchlab.skie.sir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirClassParent
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.oir.OirProvider
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.util.swift.toValidSwiftIdentifier

class ClassNamespaceProvider(
    kirProvider: KirProvider,
    private val oirProvider: OirProvider,
    private val sirProvider: SirProvider,
) {

    private val namespaceClassCache = mutableMapOf<KirClass, SirClass>()

    private val moduleNamespaceCache = mutableMapOf<KirModule, SirClass>()

    private val classNamespaceFile by lazy {
        sirProvider.fileProvider.getIrFile(oirProvider.skieModule.name, "Namespace")
    }

    private val classNamespaceBaseClass: SirClass by lazy {
        SirClass(
            baseName = "Skie",
            parent = classNamespaceFile,
            kind = SirClass.Kind.Enum,
        )
    }

    private val kotlinModulesWithShortNameCollision =
        kirProvider.kotlinModules
            .groupBy { it.shortNamespaceModuleName }
            .filter { it.value.size > 1 }
            .values
            .flatten()
            .toSet()

    fun getNamespaceFile(kirClass: KirClass): SirIrFile =
        sirProvider.fileProvider.getIrFile(kirClass.skieFileNamespaceName, kirClass.skieFileName)

    fun getNamespace(kirClass: KirClass): SirExtension =
        sirProvider.getExtension(
            classDeclaration = getNamespaceClass(kirClass),
            parent = getNamespaceFile(kirClass),
        )

    fun getNamespaceClass(kirClass: KirClass): SirClass =
        namespaceClassCache.getOrPut(kirClass) {
            SirClass(
                baseName = kirClass.classNamespaceSimpleName,
                parent = getNamespaceClass(kirClass.parent),
                kind = SirClass.Kind.Enum,
            )
        }

    private fun getNamespaceClass(classParent: KirClassParent): SirClass =
        when (classParent) {
            is KirClass -> this.getNamespaceClass(classParent)
            is KirModule -> getModuleNamespace(classParent)
        }

    private fun getModuleNamespace(module: KirModule): SirClass =
        moduleNamespaceCache.getOrPut(module) {
            val sirClass = SirClass(
                baseName = module.namespaceModuleName,
                parent = classNamespaceBaseClass,
                kind = SirClass.Kind.Enum,
            )

            if (!module.shortNameCollides) {
                SirTypeAlias(
                    baseName = module.fullNamespaceModuleName,
                    parent = classNamespaceBaseClass,
                ) {
                    sirClass.defaultType
                }
            }

            sirClass
        }

    private val KirModule.namespaceModuleName: String
        get() {
            val canUseShortName = !this.shortNameCollides && shortNamespaceModuleName != sirProvider.skieModule.name.toValidSwiftIdentifier()

            return if (canUseShortName) this.shortNamespaceModuleName else this.fullNamespaceModuleName
        }

    private val KirClass.skieFileNamespaceName: String
        get() {
            val isProducedBySkie = when (this.module.origin) {
                KirModule.Origin.SkieRuntime, KirModule.Origin.SkieGenerated -> true
                KirModule.Origin.Kotlin, KirModule.Origin.External -> false
            }

            return if (isProducedBySkie) oirProvider.skieModule.name else module.namespaceModuleName
        }

    private val KirModule.shortNameCollides: Boolean
        get() = this in kotlinModulesWithShortNameCollision

    private val KirClass.classNamespaceSimpleName: String
        get() = this.kotlinIdentifier.toValidSwiftIdentifier()

    @Suppress("RecursivePropertyAccessor")
    private val KirClass.skieFileName: String
        get() = ((this.parent as? KirClass)?.skieFileName?.let { "$it." } ?: "") + this.classNamespaceSimpleName

    private val KirModule.shortNamespaceModuleName: String
        get() = this.name
            .substringAfter(":")
            .changeNamingConventionToPascalCase()
            .toValidSwiftIdentifier()

    private val KirModule.fullNamespaceModuleName: String
        get() = this.name
            .replace(":", "__")
            .toValidSwiftIdentifier()
}

private fun String.changeNamingConventionToPascalCase(): String =
    splitToSequence("_", "-")
        .map { it.replaceFirstChar(Char::uppercase) }
        .joinToString("")
