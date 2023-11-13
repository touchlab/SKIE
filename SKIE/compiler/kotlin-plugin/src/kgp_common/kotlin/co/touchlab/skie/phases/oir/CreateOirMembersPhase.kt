@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.oir

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirOverridableDeclaration
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.oir.element.OirCallableDeclarationParent
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirConstructor
import co.touchlab.skie.oir.element.OirExtension
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.oir.element.OirProperty
import co.touchlab.skie.oir.element.OirScope
import co.touchlab.skie.oir.element.OirSimpleFunction
import co.touchlab.skie.oir.element.OirValueParameter
import co.touchlab.skie.oir.type.translation.OirTypeParameterScope
import co.touchlab.skie.oir.type.translation.typeParameterScope
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.oir.util.getOirValueParameterName
import co.touchlab.skie.util.swift.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.konan.cKeywords

class CreateOirMembersPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider
    private val namer = context.namer
    private val oirTypeTranslator = context.oirTypeTranslator

    private val extensionCache = mutableMapOf<OirClass, OirExtension>()

    context(SirPhase.Context)
    override fun execute() {
        createAllMembers()
        initializeOverridesForAllMembers()
    }

    private fun createAllMembers() {
        kirProvider.allCallableDeclarations.forEach(::createCallableDeclaration)
    }

    private fun createCallableDeclaration(kirCallableDeclaration: KirCallableDeclaration<*>) {
        when (kirCallableDeclaration) {
            is KirConstructor -> createConstructor(kirCallableDeclaration)
            is KirSimpleFunction -> createFunction(kirCallableDeclaration)
            is KirProperty -> createProperty(kirCallableDeclaration)
        }
    }

    private fun createConstructor(constructor: KirConstructor) {
        val oirConstructor = OirConstructor(
            selector = namer.getSelector(constructor.baseDescriptor),
            parent = constructor.owner.oirClass,
            errorHandlingStrategy = constructor.errorHandlingStrategy,
        )

        createValueParameters(constructor, oirConstructor)

        constructor.oirConstructor = oirConstructor
    }

    private fun createFunction(function: KirSimpleFunction) {
        val oirSimpleFunction = OirSimpleFunction(
            selector = namer.getSelector(function.baseDescriptor),
            parent = getOirCallableDeclarationParent(function),
            scope = function.oirScope,
            returnType = oirTypeTranslator.mapType(function.returnType, function.owner.oirClass.genericsScope),
            errorHandlingStrategy = function.errorHandlingStrategy,
        )

        createValueParameters(function, oirSimpleFunction)

        function.oirSimpleFunction = oirSimpleFunction
    }

    private fun createProperty(property: KirProperty) {
        val oirProperty = OirProperty(
            name = namer.getPropertyName(property.baseDescriptor).objCName,
            type = oirTypeTranslator.mapType(property.type, property.owner.oirClass.genericsScope),
            isVar = property.isVar,
            parent = getOirCallableDeclarationParent(property),
            scope = property.oirScope,
        )

        property.oirProperty = oirProperty
    }

    private fun getOirCallableDeclarationParent(kirCallableDeclaration: KirCallableDeclaration<*>): OirCallableDeclarationParent =
        if (kirCallableDeclaration.origin == KirCallableDeclaration.Origin.Extension && kirCallableDeclaration.owner.kind != KirClass.Kind.File) {
            getOrCreateExtension(kirCallableDeclaration.owner.oirClass)
        } else {
            kirCallableDeclaration.owner.oirClass
        }

    private fun getOrCreateExtension(oirClass: OirClass): OirExtension =
        extensionCache.getOrPut(oirClass) {
            OirExtension(
                classDeclaration = oirClass,
                parent = oirClass.parent,
            )
        }

    private fun createValueParameters(function: KirFunction<*>, oirFunction: OirFunction) {
        val valueParameters = function.valueParameters
        if (valueParameters.isEmpty()) {
            return
        }

        val labels = listOf("") + oirFunction.selector.trimEnd(':').split(':').drop(1)
        val usedNames = mutableSetOf<String>()

        val oirGenericsScope = oirFunction.owner.genericsScope

        valueParameters.forEachIndexed { index, valueParameter ->
            val oirValueParameter = OirValueParameter(
                label = labels[index],
                name = getValueParameterName(valueParameter, usedNames),
                type = oirTypeTranslator.mapType(valueParameter.type, oirGenericsScope),
                parent = oirFunction,
                index = index,
            )

            valueParameter.oirValueParameter = oirValueParameter
        }
    }

    private fun getValueParameterName(kirValueParameter: KirValueParameter, usedNames: MutableSet<String>): String {
        val candidateName = when (kirValueParameter.kind) {
            is KirValueParameter.Kind.ValueParameter -> namer.getOirValueParameterName(kirValueParameter.kind.descriptor)
            else -> kirValueParameter.name
        }

        var uniqueName = candidateName.toValidSwiftIdentifier()
        while (uniqueName in usedNames || uniqueName in cKeywords) {
            uniqueName += "_"
        }

        usedNames.add(uniqueName)

        return uniqueName
    }

    private fun initializeOverridesForAllMembers() {
        kirProvider.allOverridableDeclaration.forEach(::initializeOverrides)
    }

    private fun initializeOverrides(overridableDeclaration: KirOverridableDeclaration<*, *>) {
        when (overridableDeclaration) {
            is KirSimpleFunction -> initializeOverrides(overridableDeclaration)
            is KirProperty -> initializeOverrides(overridableDeclaration)
        }
    }

    private fun initializeOverrides(kirSimpleFunction: KirSimpleFunction) {
        kirSimpleFunction.overriddenDeclarations.forEach { overriddenDeclaration ->
            kirSimpleFunction.oirSimpleFunction.addOverride(overriddenDeclaration.oirSimpleFunction)
        }
    }

    private fun initializeOverrides(kirProperty: KirProperty) {
        kirProperty.overriddenDeclarations.forEach { overriddenDeclaration ->
            kirProperty.oirProperty.addOverride(overriddenDeclaration.oirProperty)
        }
    }

    private val KirOverridableDeclaration<*, *>.oirScope: OirScope
        get() = if (owner.kind == KirClass.Kind.File) {
            OirScope.Static
        } else {
            OirScope.Member
        }

    private val OirClass.genericsScope: OirTypeParameterScope
        get() = kirProvider.getClass(this).typeParameterScope
}
