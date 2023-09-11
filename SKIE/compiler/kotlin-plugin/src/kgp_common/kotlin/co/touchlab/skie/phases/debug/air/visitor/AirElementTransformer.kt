package co.touchlab.skie.phases.debug.air.visitor

import co.touchlab.skie.phases.debug.air.element.AirAnonymousInitializer
import co.touchlab.skie.phases.debug.air.element.AirClass
import co.touchlab.skie.phases.debug.air.element.AirConstant
import co.touchlab.skie.phases.debug.air.element.AirConstantArray
import co.touchlab.skie.phases.debug.air.element.AirConstantClassReference
import co.touchlab.skie.phases.debug.air.element.AirConstantEnumReference
import co.touchlab.skie.phases.debug.air.element.AirConstantErased
import co.touchlab.skie.phases.debug.air.element.AirConstantObject
import co.touchlab.skie.phases.debug.air.element.AirConstantPrimitive
import co.touchlab.skie.phases.debug.air.element.AirConstructor
import co.touchlab.skie.phases.debug.air.element.AirDeclaration
import co.touchlab.skie.phases.debug.air.element.AirElement
import co.touchlab.skie.phases.debug.air.element.AirEnumEntry
import co.touchlab.skie.phases.debug.air.element.AirField
import co.touchlab.skie.phases.debug.air.element.AirFile
import co.touchlab.skie.phases.debug.air.element.AirFunction
import co.touchlab.skie.phases.debug.air.element.AirModule
import co.touchlab.skie.phases.debug.air.element.AirProject
import co.touchlab.skie.phases.debug.air.element.AirProperty
import co.touchlab.skie.phases.debug.air.element.AirSimpleFunction
import co.touchlab.skie.phases.debug.air.element.AirTypeAlias
import co.touchlab.skie.phases.debug.air.element.AirTypeParameter
import co.touchlab.skie.phases.debug.air.element.AirValueParameter

interface AirElementTransformer<in D> :
    AirElementVisitor<AirElement, D> {

    override fun visitElement(element: AirElement, data: D): AirElement =
        throwNotImplemented()

    override fun visitProject(project: AirProject, data: D): AirProject =
        project.copy(
            modules = project.modules.map { it.transform(this, data) },
        )

    override fun visitModule(module: AirModule, data: D): AirModule =
        module.copy(
            files = module.files.map { it.transform(this, data) },
        )

    override fun visitFile(file: AirFile, data: D): AirFile =
        file.copy(
            declarations = file.declarations.map { it.transform(this, data) },
            annotations = file.annotations.map { it.transform(this, data) },
        )

    override fun visitDeclaration(
        declaration: AirDeclaration,
        data: D,
    ): AirDeclaration =
        throwNotImplemented()

    override fun visitValueParameter(
        valueParameter: AirValueParameter,
        data: D,
    ): AirValueParameter =
        valueParameter.copy(
            annotations = valueParameter.annotations.map { it.transform(this, data) },
        )

    override fun visitClass(airClass: AirClass, data: D): AirClass =
        airClass.copy(
            annotations = airClass.annotations.map { it.transform(this, data) },
            typeParameters = airClass.typeParameters.map { it.transform(this, data) },
            declarations = airClass.declarations.map { it.transform(this, data) },
            enumEntries = airClass.enumEntries.map { it.transform(this, data) },
        )

    override fun visitEnumEntry(enumEntry: AirEnumEntry, data: D): AirEnumEntry =
        enumEntry.copy(
            enumEntryClass = enumEntry.enumEntryClass?.transform(this, data),
            annotations = enumEntry.annotations.map { it.transform(this, data) },
        )

    override fun visitTypeParameter(
        typeParameter: AirTypeParameter,
        data: D,
    ): AirTypeParameter =
        typeParameter.copy(
            annotations = typeParameter.annotations.map { it.transform(this, data) },
        )

    override fun visitAnonymousInitializer(
        anonymousInitializer: AirAnonymousInitializer,
        data: D,
    ): AirAnonymousInitializer =
        anonymousInitializer.copy(
            annotations = anonymousInitializer.annotations.map { it.transform(this, data) },
        )

    override fun visitFunction(function: AirFunction, data: D): AirFunction =
        throwNotImplemented()

    @Suppress("DuplicatedCode")
    override fun visitConstructor(
        constructor: AirConstructor,
        data: D,
    ): AirConstructor =
        constructor.copy(
            annotations = constructor.annotations.map { it.transform(this, data) },
            dispatchReceiverParameter = constructor.dispatchReceiverParameter?.transform(this, data),
            extensionReceiverParameter = constructor.extensionReceiverParameter?.transform(this, data),
            valueParameters = constructor.valueParameters.map { it.transform(this, data) },
            typeParameters = constructor.typeParameters.map { it.transform(this, data) },
        )

    override fun visitProperty(property: AirProperty, data: D): AirProperty =
        property.copy(
            annotations = property.annotations.map { it.transform(this, data) },
            backingField = property.backingField?.transform(this, data),
            getter = property.getter?.transform(this, data),
            setter = property.setter?.transform(this, data),
        )

    override fun visitField(field: AirField, data: D): AirField =
        field.copy(
            annotations = field.annotations.map { it.transform(this, data) },
        )

    @Suppress("DuplicatedCode")
    override fun visitSimpleFunction(
        simpleFunction: AirSimpleFunction,
        data: D,
    ): AirSimpleFunction =
        simpleFunction.copy(
            annotations = simpleFunction.annotations.map { it.transform(this, data) },
            dispatchReceiverParameter = simpleFunction.dispatchReceiverParameter?.transform(this, data),
            extensionReceiverParameter = simpleFunction.extensionReceiverParameter?.transform(this, data),
            valueParameters = simpleFunction.valueParameters.map { it.transform(this, data) },
            typeParameters = simpleFunction.typeParameters.map { it.transform(this, data) },
        )

    override fun visitConstant(constant: AirConstant, data: D): AirConstant =
        throwNotImplemented()

    override fun visitConstantArray(
        constantArray: AirConstantArray,
        data: D,
    ): AirConstantArray =
        constantArray.copy(
            elements = constantArray.elements.map { it.transform(this, data) },
        )

    override fun visitConstantClassReference(
        constantClassReference: AirConstantClassReference,
        data: D,
    ): AirConstantClassReference =
        constantClassReference

    override fun visitConstantEnumReference(
        constantEnumReference: AirConstantEnumReference,
        data: D,
    ): AirConstantEnumReference =
        constantEnumReference

    override fun visitConstantErased(
        constantErased: AirConstantErased,
        data: D,
    ): AirConstantErased =
        constantErased

    override fun visitConstantObject(
        constantObject: AirConstantObject,
        data: D,
    ): AirConstantObject =
        constantObject.copy(
            valueArguments = constantObject.valueArguments.map { it.transform(this, data) },
        )

    override fun visitConstantPrimitive(
        constantPrimitive: AirConstantPrimitive,
        data: D,
    ): AirConstantPrimitive =
        constantPrimitive

    override fun visitTypeAlias(typeAlias: AirTypeAlias, data: D): AirTypeAlias =
        typeAlias.copy(
            annotations = typeAlias.annotations.map { it.transform(this, data) },
            typeParameters = typeAlias.typeParameters.map { it.transform(this, data) },
        )

    private fun throwNotImplemented(): Nothing {
        throw UnsupportedOperationException("Each child should have direct implementation.")
    }
}
