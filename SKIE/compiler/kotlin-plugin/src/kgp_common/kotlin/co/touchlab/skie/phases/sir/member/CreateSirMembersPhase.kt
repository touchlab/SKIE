package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirConstructor
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
import co.touchlab.skie.util.collisionFreeIdentifier
import co.touchlab.skie.util.swift.toValidSwiftIdentifier

class CreateSirMembersPhase(
    val context: SirPhase.Context,
) : SirPhase {

    private val namer = context.namer
    private val sirProvider = context.sirProvider
    private val sirTypeTranslator = context.sirTypeTranslator

    context(SirPhase.Context)
    override fun execute() {
        kirProvider.allCallableDeclarations.forEach(::createCallableDeclaration)
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
            throws = function.errorHandlingStrategy.isThrowing,
            deprecationLevel = function.deprecationLevel,
        ).apply {
            createValueParameters(function, swiftFunctionName)
        }

        function.oirSimpleFunction.originalSirFunction = sirFunction
    }

    private fun createProperty(property: KirProperty) {
        val oirProperty = property.oirProperty

        val sirProperty = SirProperty(
            identifier = namer.getPropertyName(property.baseDescriptor).swiftName,
            parent = property.getSirParent(),
            type = sirTypeTranslator.mapType(oirProperty.type),
            scope = oirProperty.scope.sirScope,
            deprecationLevel = property.deprecationLevel,
        ).apply {
            property.descriptor.getter?.let {
                SirGetter(
                    throws = false,
                )
            }
            property.descriptor.setter?.let {
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
            is OirExtension -> sirProvider.getExtension(parent.classDeclaration.originalSirClass, parent.classDeclaration.originalSirClass.module)
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
                    name = valueParameter.name.toValidSwiftIdentifier().collisionFreeIdentifier(usedParameterNames),
                    type = sirTypeTranslator.mapType(oirValueParameter.type, isEscaping = true),
                )

                usedParameterNames.add(sirValueParameter.name)

                oirValueParameter.originalSirValueParameter = sirValueParameter
            }
    }

    private fun KirFunction<*>.getExportedValueParameters()
        : List<KirValueParameter> =
        this.valueParameters.filter {
            when (it.kind) {
                KirValueParameter.Kind.ErrorOut -> false
                else -> true
            }
        }

    private val KirFunction<*>.swiftFunctionName
        : SwiftFunctionName
        get() {
            val swiftName = namer.getSwiftName(this.baseDescriptor)

            val (identifier, argumentLabelsString) = swiftNameComponentsRegex.matchEntire(swiftName)?.destructured
                ?: error("Unable to parse swift name: $swiftName")

            val argumentLabels = argumentLabelsString.split(":").map { it.trim() }.filter { it.isNotEmpty() }

            return SwiftFunctionName(identifier, argumentLabels)
        }

    private data
    class SwiftFunctionName(
        val identifier: String,
        val argumentLabels
        : List<String>,
    )

    private companion object {

        val swiftNameComponentsRegex = "(.+?)\\((.*?)\\)".toRegex()
    }
}
