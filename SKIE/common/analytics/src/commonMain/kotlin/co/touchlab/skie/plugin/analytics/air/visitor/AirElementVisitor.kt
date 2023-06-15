package co.touchlab.skie.plugin.analytics.air.visitor

import co.touchlab.skie.plugin.analytics.air.element.AirAnonymousInitializer
import co.touchlab.skie.plugin.analytics.air.element.AirClass
import co.touchlab.skie.plugin.analytics.air.element.AirConstant
import co.touchlab.skie.plugin.analytics.air.element.AirConstantArray
import co.touchlab.skie.plugin.analytics.air.element.AirConstantClassReference
import co.touchlab.skie.plugin.analytics.air.element.AirConstantEnumReference
import co.touchlab.skie.plugin.analytics.air.element.AirConstantErased
import co.touchlab.skie.plugin.analytics.air.element.AirConstantObject
import co.touchlab.skie.plugin.analytics.air.element.AirConstantPrimitive
import co.touchlab.skie.plugin.analytics.air.element.AirConstructor
import co.touchlab.skie.plugin.analytics.air.element.AirDeclaration
import co.touchlab.skie.plugin.analytics.air.element.AirElement
import co.touchlab.skie.plugin.analytics.air.element.AirEnumEntry
import co.touchlab.skie.plugin.analytics.air.element.AirField
import co.touchlab.skie.plugin.analytics.air.element.AirFile
import co.touchlab.skie.plugin.analytics.air.element.AirFunction
import co.touchlab.skie.plugin.analytics.air.element.AirModule
import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.air.element.AirProperty
import co.touchlab.skie.plugin.analytics.air.element.AirSimpleFunction
import co.touchlab.skie.plugin.analytics.air.element.AirTypeAlias
import co.touchlab.skie.plugin.analytics.air.element.AirTypeParameter
import co.touchlab.skie.plugin.analytics.air.element.AirValueParameter

interface AirElementVisitor<out R, in D> {

    fun visitElement(element: AirElement, data: D): R

    fun visitProject(project: AirProject, data: D): R =
        visitElement(project, data)

    fun visitModule(module: AirModule, data: D): R =
        visitElement(module, data)

    fun visitFile(file: AirFile, data: D): R =
        visitElement(file, data)

    fun visitDeclaration(declaration: AirDeclaration, data: D): R =
        visitElement(declaration, data)

    fun visitValueParameter(valueParameter: AirValueParameter, data: D): R =
        visitElement(valueParameter, data)

    fun visitClass(airClass: AirClass, data: D): R =
        visitDeclaration(airClass, data)

    fun visitEnumEntry(enumEntry: AirEnumEntry, data: D): R =
        visitElement(enumEntry, data)

    fun visitTypeParameter(typeParameter: AirTypeParameter, data: D): R =
        visitElement(typeParameter, data)

    fun visitAnonymousInitializer(anonymousInitializer: AirAnonymousInitializer, data: D): R =
        visitDeclaration(anonymousInitializer, data)

    fun visitFunction(function: AirFunction, data: D): R =
        visitDeclaration(function, data)

    fun visitConstructor(constructor: AirConstructor, data: D): R =
        visitFunction(constructor, data)

    fun visitProperty(property: AirProperty, data: D): R =
        visitDeclaration(property, data)

    fun visitField(field: AirField, data: D): R =
        visitDeclaration(field, data)

    fun visitSimpleFunction(simpleFunction: AirSimpleFunction, data: D): R =
        visitFunction(simpleFunction, data)

    fun visitConstant(constant: AirConstant, data: D): R =
        visitElement(constant, data)

    fun visitConstantArray(constantArray: AirConstantArray, data: D): R =
        visitConstant(constantArray, data)

    fun visitConstantClassReference(constantClassReference: AirConstantClassReference, data: D): R =
        visitConstant(constantClassReference, data)

    fun visitConstantEnumReference(constantEnumReference: AirConstantEnumReference, data: D): R =
        visitConstant(constantEnumReference, data)

    fun visitConstantErased(constantErased: AirConstantErased, data: D): R =
        visitConstant(constantErased, data)

    fun visitConstantObject(constantObject: AirConstantObject, data: D): R =
        visitConstant(constantObject, data)

    fun visitConstantPrimitive(constantPrimitive: AirConstantPrimitive, data: D): R =
        visitConstant(constantPrimitive, data)

    fun visitTypeAlias(typeAlias: AirTypeAlias, data: D): R =
        visitDeclaration(typeAlias, data)
}
