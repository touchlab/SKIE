package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.configuration.SkieVisibility
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirExtension
import co.touchlab.skie.oir.element.OirScope
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclarationNamespace
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.util.collisionFreeIdentifier
import co.touchlab.skie.util.swift.toValidSwiftIdentifier
import co.touchlab.skie.util.toSirVisibility

class CreateSirMembersPhase(
    val context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider
    private val sirProvider = context.sirProvider
    private val sirTypeTranslator = context.sirTypeTranslator

    context(SirPhase.Context)
    override suspend fun execute() {
        createAllMembers()
        createAllEnumEntries()
    }

    private fun createAllMembers() {
        kirProvider.kotlinCallableDeclarations.forEach(::createCallableDeclaration)
    }

    private fun createCallableDeclaration(kirCallableDeclaration: KirCallableDeclaration<*>) {
        when (kirCallableDeclaration) {
            is KirConstructor -> createConstructor(kirCallableDeclaration)
            is KirProperty -> createProperty(kirCallableDeclaration)
            is KirSimpleFunction -> createFunction(kirCallableDeclaration)
        }
    }

    private fun createConstructor(constructor: KirConstructor) {
        val sirConstructor = SirConstructor(
            parent = constructor.getSirParent(),
            throws = constructor.errorHandlingStrategy.isThrowing,
            deprecationLevel = constructor.deprecationLevel,
            visibility = constructor.visibility,
            isHidden = constructor.isHidden,
        ).apply {
            createValueParameters(constructor, constructor.swiftFunctionName)
        }

        constructor.oirConstructor.originalSirConstructor = sirConstructor
    }

    private fun createFunction(function: KirSimpleFunction) {
        val oirSimpleFunction = function.oirSimpleFunction
        val swiftFunctionName = function.swiftFunctionName

        val sirFunction = SirSimpleFunction(
            identifier = swiftFunctionName.identifier,
            parent = function.getSirParent(),
            returnType = sirTypeTranslator.mapReturnType(oirSimpleFunction.returnType, function.errorHandlingStrategy),
            scope = oirSimpleFunction.scope.sirScope,
            isFakeOverride = function.isFakeOverride,
            throws = function.errorHandlingStrategy.isThrowing,
            deprecationLevel = function.deprecationLevel,
            visibility = function.visibility,
            isReplaced = function.isReplaced,
            isHidden = function.isHidden,
        ).apply {
            createValueParameters(function, swiftFunctionName)
        }

        function.oirSimpleFunction.originalSirFunction = sirFunction
    }

    private fun createProperty(property: KirProperty) {
        val oirProperty = property.oirProperty

        val sirProperty = SirProperty(
            identifier = property.swiftName,
            parent = property.getSirParent(),
            type = sirTypeTranslator.mapType(oirProperty.type),
            scope = oirProperty.scope.sirScope,
            deprecationLevel = property.deprecationLevel,
            visibility = property.visibility,
            isReplaced = property.isReplaced,
            isHidden = property.isHidden,
            isFakeOverride = property.isFakeOverride,
        ).apply {
            SirGetter(
                throws = false,
            )

            if (property.isVar) {
                SirSetter(
                    throws = false,
                )
            }
        }

        oirProperty.originalSirProperty = sirProperty
    }

    private val OirScope.sirScope: SirScope
        get() = when (this) {
            OirScope.Member -> SirScope.Member
            OirScope.Static -> SirScope.Static
        }

    private fun KirCallableDeclaration<*>.getSirParent(): SirDeclarationNamespace =
        when (val parent = oirCallableDeclaration.parent) {
            is OirClass -> parent.originalSirClass
            is OirExtension -> parent.sirExtension
        }

    private fun SirFunction.createValueParameters(
        kirFunction: KirFunction<*>,
        swiftFunctionName: SwiftFunctionName,
    ) {
        val valueParameters = kirFunction.getExportedValueParameters()
        if (valueParameters.isEmpty()) {
            return
        }

        val usedParameterNames = mutableListOf<String>()

        valueParameters
            .zip(swiftFunctionName.argumentLabels)
            .forEach { (valueParameter, argumentLabel) ->
                val oirValueParameter = valueParameter.oirValueParameter

                val sirValueParameter = SirValueParameter(
                    label = argumentLabel,
                    name = valueParameter.kotlinName.toValidSwiftIdentifier().collisionFreeIdentifier(usedParameterNames),
                    type = sirTypeTranslator.mapType(oirValueParameter.type, isEscaping = true),
                )

                usedParameterNames.add(sirValueParameter.name)

                oirValueParameter.originalSirValueParameter = sirValueParameter
            }
    }

    private fun KirFunction<*>.getExportedValueParameters(): List<KirValueParameter> =
        this.valueParameters.filter {
            when (it.kind) {
                KirValueParameter.Kind.ErrorOut -> false
                else -> true
            }
        }

    private val KirCallableDeclaration<*>.visibility: SirVisibility
        get() = this.configuration[SkieVisibility].toSirVisibility()

    private val KirCallableDeclaration<*>.isReplaced: Boolean
        get() = this.isRefinedInSwift || this.configuration[SkieVisibility] == SkieVisibility.Level.PublicButReplaced

    private val KirCallableDeclaration<*>.isHidden: Boolean
        get() = (this.visibility == SirVisibility.Public && this.isRefinedInSwift) ||
            this.configuration[SkieVisibility] in listOf(SkieVisibility.Level.PublicButHidden, SkieVisibility.Level.PublicButReplaced)

    private val KirFunction<*>.swiftFunctionName: SwiftFunctionName
        get() {
            val (identifier, argumentLabelsString) = swiftNameComponentsRegex.matchEntire(this.swiftName)?.destructured
                ?: error("Unable to parse swift name: ${this.swiftName}")

            val argumentLabels = argumentLabelsString.split(":").map { it.trim() }.filter { it.isNotEmpty() }

            return SwiftFunctionName(identifier, argumentLabels)
        }

    private fun createAllEnumEntries() {
        kirProvider.kotlinEnums.forEach(::createEnumEntries)
    }

    private fun createEnumEntries(kirClass: KirClass) {
        kirClass.enumEntries.forEach(::createEnumEntry)
    }

    private fun createEnumEntry(enumEntry: KirEnumEntry) {
        val oirEnumEntry = enumEntry.oirEnumEntry

        oirEnumEntry.originalSirProperty = SirProperty(
            identifier = enumEntry.swiftName,
            parent = enumEntry.owner.originalSirClass,
            type = sirTypeTranslator.mapType(oirEnumEntry.type),
            scope = oirEnumEntry.scope.sirScope,
            deprecationLevel = oirEnumEntry.deprecationLevel,
        ).apply {
            SirGetter(
                throws = false,
            )
        }
    }

    private data class SwiftFunctionName(
        val identifier: String,
        val argumentLabels
        : List<String>,
    )

    private companion object {

        val swiftNameComponentsRegex = "(.+?)\\((.*?)\\)".toRegex()
    }
}
