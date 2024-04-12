package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.sir.type.hasStableNameTypeAlias
import co.touchlab.skie.phases.util.MustBeExecutedAfterBridgingConfiguration
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isExported
import co.touchlab.skie.sir.element.oirClassOrNull
import co.touchlab.skie.sir.getExtension

object ExhaustiveEnumsGenerator : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses
            .filter { it.isSupported }
            .forEach {
                generate(it)
            }
    }

    context(SirPhase.Context)
    private val KirClass.isSupported: Boolean
        get() = this.originalSirClass.isExported &&
            this.kind == KirClass.Kind.Enum &&
            this.isEnumInteropEnabled

    context(SirPhase.Context)
    private val KirClass.isEnumInteropEnabled: Boolean
        get() = this.configuration[EnumInterop.Enabled]

    context(SirPhase.Context)
    private fun generate(kirClass: KirClass) {
        val skieClass = kirClass.generateBridge()

        createStableNameTypeAliasIfRequested(skieClass, kirClass)

        kirClass.configureBridging(skieClass)
    }

    @MustBeExecutedAfterBridgingConfiguration
    object NestedTypeDeclarationsPhase : StatefulSirPhase()
}

private fun KirClass.configureBridging(skieClass: SirClass) {
    bridgedSirClass = skieClass

    originalSirClass.visibility = SirVisibility.PublicButReplaced
}

context(SirPhase.Context)
private fun KirClass.generateBridge(): SirClass {
    val bridgedEnum = createBridgingEnum(this)

    addConversionExtensions(bridgedEnum)

    return bridgedEnum
}

context(SirPhase.Context)
private fun createBridgingEnum(enumKirClass: KirClass): SirClass =
    SirClass(
        baseName = enumKirClass.originalSirClass.baseName,
        parent = enumKirClass.originalSirClass.namespace?.let { namespace ->
            sirProvider.getExtension(
                classDeclaration = namespace.classDeclaration,
                parent = classNamespaceProvider.getNamespaceFile(enumKirClass),
            )
        } ?: classNamespaceProvider.getNamespaceFile(enumKirClass),
        kind = SirClass.Kind.Enum,
    ).apply {
        addEnumCases(enumKirClass)

        superTypes += listOf(
            sirBuiltins.Swift.Hashable.defaultType,
            sirBuiltins.Swift.CaseIterable.defaultType,
            sirBuiltins.Swift._ObjectiveCBridgeable.defaultType,
        )

        attributes.add("frozen")

        ExhaustiveEnumsMembersPassthroughGenerator.generatePassthroughForMembers(enumKirClass, this)
        addObjcBridgeableImplementation(enumKirClass)

        doInPhase(ExhaustiveEnumsGenerator.NestedTypeDeclarationsPhase) {
            addNestedClassTypeAliases(enumKirClass)
            addCompanionObjectPropertyIfNeeded(enumKirClass)
        }
    }

private fun SirClass.addEnumCases(enum: KirClass) {
    enum.enumEntries.forEach {
        SirEnumCase(
            simpleName = it.sirEnumEntry.name,
        )
    }
}

private fun SirClass.addNestedClassTypeAliases(enum: KirClass) {
    enum.originalSirClass.declarations
        .filterIsInstance<SirClass>()
        .forEach { nestedClass ->
            addNestedClassTypeAlias(nestedClass)

            nestedClass.oirClassOrNull?.bridgedSirClass?.let { addNestedClassTypeAlias(it) }
        }
}

private fun SirClass.addNestedClassTypeAlias(nestedClass: SirClass) {
    SirTypeAlias(
        baseName = nestedClass.baseName,
        visibility = nestedClass.visibility,
    ) {
        nestedClass.defaultType
    }
}

private fun SirClass.addCompanionObjectPropertyIfNeeded(enum: KirClass) {
    val companion = enum.companionObject ?: return

    SirProperty(
        identifier = "companion",
        type = companion.primarySirClass.defaultType,
        scope = SirScope.Static,
    ).apply {
        SirGetter().bodyBuilder.add {
            // TODO Refactor and use SirProperty reference once Sir contains the shared property for companion objects
            addStatement("return ${companion.primarySirClass.defaultType.evaluate().swiftPoetTypeName.name}.shared")
        }
    }
}

context(SirPhase.Context)
private fun KirClass.addConversionExtensions(bridgedEnum: SirClass) {
    classNamespaceProvider.getNamespaceFile(this).apply {
        addToKotlinConversionExtension(originalSirClass, bridgedEnum)
        addToSwiftConversionExtension(originalSirClass, bridgedEnum)
    }
}

context(SirPhase.Context)
private fun SirIrFile.addToKotlinConversionExtension(enum: SirClass, bridgedEnum: SirClass) {
    this.getExtension(
        classDeclaration = bridgedEnum,
    ).apply {
        addToKotlinConversionMethod(enum)
    }
}

private fun SirExtension.addToKotlinConversionMethod(enum: SirClass) {
    SirSimpleFunction(
        identifier = "toKotlinEnum",
        returnType = enum.defaultType,
    ).apply {
        bodyBuilder.add {
            addStatement("return _bridgeToObjectiveC()")
        }
    }
}

context(SirPhase.Context)
private fun SirIrFile.addToSwiftConversionExtension(enum: SirClass, bridgedEnum: SirClass) {
    this.getExtension(
        classDeclaration = enum,
    ).apply {
        addToSwiftConversionMethod(bridgedEnum)
    }
}

private fun SirExtension.addToSwiftConversionMethod(bridgedEnum: SirClass) {
    SirSimpleFunction(
        identifier = "toSwiftEnum",
        returnType = bridgedEnum.defaultType,
    ).apply {
        bodyBuilder.add {
            addStatement("return %T._unconditionallyBridgeFromObjectiveC(self)", bridgedEnum.defaultType.evaluate().swiftPoetTypeName)
        }
    }
}

context(SirPhase.Context)
private fun createStableNameTypeAliasIfRequested(bridgedEnum: SirClass, kirClass: KirClass) {
    if (!kirClass.hasStableNameTypeAlias) {
        return
    }

    val typeAlias = SirTypeAlias(
        baseName = "Enum",
        parent = classNamespaceProvider.getNamespace(kirClass),
        visibility = SirVisibility.PublicButReplaced,
    ) {
        bridgedEnum.defaultType.withFqName()
    }

    if (SkieConfigurationFlag.Debug_UseStableTypeAliases.isEnabled) {
        bridgedEnum.internalTypeAlias = typeAlias
    }
}
