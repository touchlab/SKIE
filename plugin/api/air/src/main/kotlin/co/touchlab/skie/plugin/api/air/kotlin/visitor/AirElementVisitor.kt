package co.touchlab.skie.plugin.api.air.kotlin.visitor

import co.touchlab.skie.plugin.api.air.kotlin.element.AirAnonymousInitializer
import co.touchlab.skie.plugin.api.air.kotlin.element.AirClass
import co.touchlab.skie.plugin.api.air.kotlin.element.AirConstant
import co.touchlab.skie.plugin.api.air.kotlin.element.AirConstantArray
import co.touchlab.skie.plugin.api.air.kotlin.element.AirConstantObject
import co.touchlab.skie.plugin.api.air.kotlin.element.AirConstantPrimitive
import co.touchlab.skie.plugin.api.air.kotlin.element.AirConstructor
import co.touchlab.skie.plugin.api.air.kotlin.element.AirDeclaration
import co.touchlab.skie.plugin.api.air.kotlin.element.AirElement
import co.touchlab.skie.plugin.api.air.kotlin.element.AirEnumEntry
import co.touchlab.skie.plugin.api.air.kotlin.element.AirField
import co.touchlab.skie.plugin.api.air.kotlin.element.AirFile
import co.touchlab.skie.plugin.api.air.kotlin.element.AirFunction
import co.touchlab.skie.plugin.api.air.kotlin.element.AirModule
import co.touchlab.skie.plugin.api.air.kotlin.element.AirProject
import co.touchlab.skie.plugin.api.air.kotlin.element.AirProperty
import co.touchlab.skie.plugin.api.air.kotlin.element.AirSimpleFunction
import co.touchlab.skie.plugin.api.air.kotlin.element.AirTypeParameter
import co.touchlab.skie.plugin.api.air.kotlin.element.AirValueParameter

interface AirElementVisitor<out R, in D> {

    fun visitElement(element: AirElement, data: D): R

    fun visitProject(module: AirProject, data: D): R =
        visitElement(module, data)

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
        visitElement(field, data)

    fun visitSimpleFunction(simpleFunction: AirSimpleFunction, data: D): R =
        visitFunction(simpleFunction, data)

    fun visitConstant(constant: AirConstant, data: D): R =
        visitElement(constant, data)

    fun visitConstantArray(constantArray: AirConstantArray, data: D): R =
        visitConstant(constantArray, data)

    fun visitConstantObject(constantObject: AirConstantObject, data: D): R =
        visitConstant(constantObject, data)

    fun visitConstantPrimitive(constantPrimitive: AirConstantPrimitive, data: D): R =
        visitConstant(constantPrimitive, data)
}
