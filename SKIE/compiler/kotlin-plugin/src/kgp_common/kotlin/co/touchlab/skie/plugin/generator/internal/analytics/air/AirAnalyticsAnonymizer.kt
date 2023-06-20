package co.touchlab.skie.plugin.generator.internal.analytics.air

import co.touchlab.skie.plugin.analytics.air.element.AirClass
import co.touchlab.skie.plugin.analytics.air.element.AirConstantPrimitive
import co.touchlab.skie.plugin.analytics.air.element.AirEnumEntry
import co.touchlab.skie.plugin.analytics.air.element.AirField
import co.touchlab.skie.plugin.analytics.air.element.AirFile
import co.touchlab.skie.plugin.analytics.air.element.AirModule
import co.touchlab.skie.plugin.analytics.air.element.AirProperty
import co.touchlab.skie.plugin.analytics.air.element.AirSimpleFunction
import co.touchlab.skie.plugin.analytics.air.element.AirTypeAlias
import co.touchlab.skie.plugin.analytics.air.element.AirTypeParameter
import co.touchlab.skie.plugin.analytics.air.element.AirValueParameter
import co.touchlab.skie.plugin.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.util.redacted
import co.touchlab.skie.util.redactedIfNotNumberOrBoolean

object AirAnalyticsAnonymizer : AirElementTransformer<Unit> {

    override fun visitModule(module: AirModule, data: Unit): AirModule =
        super.visitModule(module, data)
            .copy(
                name = AirModule.Name(module.name.name.redacted())
            )

    override fun visitFile(file: AirFile, data: Unit): AirFile =
        super.visitFile(file, data)
            .copy(
                packageName = AirFile.PackageName(file.packageName.name.redacted()),
                fileName = AirFile.FileName(file.fileName.name.redacted()),
            )

    override fun visitValueParameter(valueParameter: AirValueParameter, data: Unit): AirValueParameter =
        super.visitValueParameter(valueParameter, data)
            .copy(
                name = AirValueParameter.Name(valueParameter.name.name.redacted()),
            )

    override fun visitClass(airClass: AirClass, data: Unit): AirClass =
        super.visitClass(airClass, data)
            .copy(
                name = AirClass.Name(airClass.name.name.redacted()),
            )

    override fun visitEnumEntry(enumEntry: AirEnumEntry, data: Unit): AirEnumEntry =
        super.visitEnumEntry(enumEntry, data)
            .copy(
                name = AirEnumEntry.Name(enumEntry.name.name.redacted()),
            )

    override fun visitTypeParameter(typeParameter: AirTypeParameter, data: Unit): AirTypeParameter =
        super.visitTypeParameter(typeParameter, data)
            .copy(
                name = AirTypeParameter.Name(typeParameter.name.name.redacted()),
            )

    override fun visitProperty(property: AirProperty, data: Unit): AirProperty =
        super.visitProperty(property, data)
            .copy(
                name = AirProperty.Name(property.name.name.redacted()),
            )

    override fun visitField(field: AirField, data: Unit): AirField =
        super.visitField(field, data)
            .copy(
                name = AirField.Name(field.name.name.redacted()),
            )

    override fun visitSimpleFunction(simpleFunction: AirSimpleFunction, data: Unit): AirSimpleFunction =
        super.visitSimpleFunction(simpleFunction, data)
            .copy(
                name = AirSimpleFunction.Name(simpleFunction.name.name.redacted()),
            )

    override fun visitConstantPrimitive(constantPrimitive: AirConstantPrimitive, data: Unit): AirConstantPrimitive =
        super.visitConstantPrimitive(constantPrimitive, data)
            .copy(
                value = constantPrimitive.value.redactedIfNotNumberOrBoolean(),
            )

    override fun visitTypeAlias(typeAlias: AirTypeAlias, data: Unit): AirTypeAlias =
        super.visitTypeAlias(typeAlias, data)
            .copy(
                name = AirTypeAlias.Name(typeAlias.name.name.redacted()),
            )
}
