@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.oir

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirEnumEntry
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
import co.touchlab.skie.phases.SirPhase
import org.jetbrains.kotlin.backend.konan.cKeywords

class CreateOirMembersPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider
    private val oirTypeTranslator = context.oirTypeTranslator

    private val extensionCache = mutableMapOf<OirClass, OirExtension>()

    context(SirPhase.Context)
    override suspend fun execute() {
        createAllMembers()
        createAllEnumEntries()
        initializeOverridesForAllMembers()
    }

    private fun createAllMembers() {
        kirProvider.kotlinCallableDeclarations.forEach(::createCallableDeclaration)
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
            selector = constructor.objCSelector,
            parent = constructor.owner.oirClass,
            errorHandlingStrategy = constructor.errorHandlingStrategy,
            deprecationLevel = constructor.deprecationLevel,
        )

        createValueParameters(constructor, oirConstructor)

        constructor.oirConstructor = oirConstructor
    }

    private fun createFunction(function: KirSimpleFunction) {
        val oirSimpleFunction = OirSimpleFunction(
            selector = function.objCSelector,
            parent = getOirCallableDeclarationParent(function),
            scope = function.oirScope,
            returnType = oirTypeTranslator.mapType(function.returnType),
            errorHandlingStrategy = function.errorHandlingStrategy,
            deprecationLevel = function.deprecationLevel,
            isFakeOverride = function.isFakeOverride,
        )

        createValueParameters(function, oirSimpleFunction)

        function.oirSimpleFunction = oirSimpleFunction
    }

    private fun createProperty(property: KirProperty) {
        property.oirProperty = OirProperty(
            name = property.objCName,
            type = oirTypeTranslator.mapType(property.type),
            isVar = property.isVar,
            parent = getOirCallableDeclarationParent(property),
            scope = property.oirScope,
            deprecationLevel = property.deprecationLevel,
            isFakeOverride = property.isFakeOverride,
        )
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

        valueParameters.forEachIndexed { index, valueParameter ->
            val oirValueParameter = OirValueParameter(
                label = labels[index],
                name = getValueParameterName(valueParameter, usedNames),
                type = oirTypeTranslator.mapType(valueParameter.type),
                parent = oirFunction,
                index = index,
            )

            valueParameter.oirValueParameter = oirValueParameter
        }
    }

    private fun getValueParameterName(kirValueParameter: KirValueParameter, usedNames: MutableSet<String>): String {
        var uniqueName = kirValueParameter.objCName
        while (uniqueName in usedNames || uniqueName in cKeywords) {
            uniqueName += "_"
        }

        usedNames.add(uniqueName)

        return uniqueName
    }

    private fun createAllEnumEntries() {
        kirProvider.kotlinEnums.forEach(::createEnumEntries)
    }

    private fun createEnumEntries(kirClass: KirClass) {
        kirClass.enumEntries.forEach(::createEnumEntry)
    }

    private fun createEnumEntry(enumEntry: KirEnumEntry) {
        enumEntry.oirEnumEntry = OirProperty(
            name = enumEntry.objCSelector,
            type = enumEntry.owner.oirClass.defaultType,
            isVar = false,
            parent = enumEntry.owner.oirClass,
            scope = OirScope.Static,
            deprecationLevel = DeprecationLevel.None,
            isFakeOverride = false,
        )
    }

    private fun initializeOverridesForAllMembers() {
        kirProvider.kotlinOverridableDeclaration.forEach(::initializeOverrides)
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
}
