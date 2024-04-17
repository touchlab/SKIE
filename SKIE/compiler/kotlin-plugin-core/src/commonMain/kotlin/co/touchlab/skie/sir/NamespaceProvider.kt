package co.touchlab.skie.sir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirClassParent
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.kir.element.KirModule.Origin
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirSourceFile
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.util.swift.toValidSwiftIdentifier

class NamespaceProvider(
    kirProvider: KirProvider,
    private val sirProvider: SirProvider,
    private val sirFileProvider: SirFileProvider,
) {

    private val skieDirectoryName = "Skie"

    private val namespaceClassCache = mutableMapOf<KirClass, SirClass>()

    private val moduleNamespaceCache = mutableMapOf<KirModule, SirClass>()

    private val namespaceClassDeclarationFile by lazy {
        getSkieNamespaceFile("Namespace")
    }

    private val namespaceBaseClass: SirClass by lazy {
        SirClass(
            baseName = "Skie",
            parent = namespaceClassDeclarationFile,
            kind = SirClass.Kind.Enum,
        )
    }

    private val kotlinModulesWithShortModuleNamespaceNameCollision =
        kirProvider.kotlinModules
            .groupBy { it.shortModuleNamespaceName }
            .filter { it.value.size > 1 }
            .values
            .flatten()
            .toSet()

    fun getSkieNamespaceWrittenSourceFile(name: String): SirSourceFile =
        sirFileProvider.getWrittenSourceFile(skieDirectoryName, name)

    fun getSkieNamespaceFile(name: String): SirIrFile =
        sirFileProvider.getIrFile(skieDirectoryName, name)

    fun getNamespaceFile(kirClass: KirClass): SirIrFile =
        sirFileProvider.getIrFile(kirClass.module.moduleDirectoryName, kirClass.swiftFileName)

    fun getNamespaceExtension(kirClass: KirClass): SirExtension =
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
            is KirModule -> getModuleNamespaceClass(classParent)
        }

    private fun getModuleNamespaceClass(module: KirModule): SirClass =
        moduleNamespaceCache.getOrPut(module) {
            val sirClass = SirClass(
                baseName = module.moduleNamespaceName,
                parent = namespaceBaseClass,
                kind = SirClass.Kind.Enum,
            )

            if (!module.shortModuleNamespaceNameCollides) {
                SirTypeAlias(
                    baseName = module.fullModuleNamespaceName,
                    parent = namespaceBaseClass,
                ) {
                    sirClass.defaultType
                }
            }

            sirClass
        }

    private val KirModule.moduleNamespaceName: String
        get() {
            val canUseShortName = !this.shortModuleNamespaceNameCollides

            return if (canUseShortName) this.shortModuleNamespaceName else this.fullModuleNamespaceName
        }

    private val KirModule.moduleDirectoryName: String
        get() = when (this.module.origin) {
            Origin.SkieGenerated, Origin.SkieRuntime -> skieDirectoryName
            Origin.KnownExternal, Origin.UnknownExternal, Origin.Kotlin -> moduleNamespaceName
        }

    private val KirModule.shortModuleNamespaceNameCollides: Boolean
        get() = this in kotlinModulesWithShortModuleNamespaceNameCollision

    private val KirClass.classNamespaceSimpleName: String
        get() = this.kotlinIdentifier.toValidSwiftIdentifier()

    @Suppress("RecursivePropertyAccessor")
    private val KirClass.swiftFileName: String
        get() = ((this.parent as? KirClass)?.swiftFileName?.let { "$it." } ?: "") + this.classNamespaceSimpleName

    private val KirModule.shortModuleNamespaceName: String
        get() = when (this.module.origin) {
            Origin.SkieGenerated -> "SkieGenerated"
            Origin.KnownExternal -> this.name.toValidSwiftIdentifier()
            Origin.UnknownExternal -> "_unknown_"
            Origin.SkieRuntime, Origin.Kotlin -> {
                this.name
                    .substringAfter(":")
                    .changeNamingConventionToPascalCase()
                    .toValidSwiftIdentifier()
            }
        }

    private val KirModule.fullModuleNamespaceName: String
        get() = when (this.module.origin) {
            Origin.SkieGenerated,
            Origin.KnownExternal,
            Origin.UnknownExternal,
            -> {
                this.shortModuleNamespaceName
            }
            Origin.SkieRuntime, Origin.Kotlin -> {
                this.name
                    .replace(":", "__")
                    .toValidSwiftIdentifier()
            }
        }
}

private fun String.changeNamingConventionToPascalCase(): String =
    splitToSequence("_", "-")
        .map { it.replaceFirstChar(Char::uppercase) }
        .joinToString("")
