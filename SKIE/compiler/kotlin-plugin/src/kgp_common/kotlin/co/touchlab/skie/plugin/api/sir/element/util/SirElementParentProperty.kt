package co.touchlab.skie.plugin.api.sir.element.util

import co.touchlab.skie.plugin.api.sir.element.SirDeclaration
import co.touchlab.skie.plugin.api.sir.element.SirDeclarationParent
import co.touchlab.skie.plugin.api.sir.element.SirEnumCase
import co.touchlab.skie.plugin.api.sir.element.SirEnumCaseAssociatedValue
import co.touchlab.skie.plugin.api.sir.element.SirFile
import co.touchlab.skie.plugin.api.sir.element.SirModule
import co.touchlab.skie.plugin.api.sir.element.SirTypeParameter
import co.touchlab.skie.plugin.api.sir.element.SirTypeParameterParent
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T : SirDeclarationParent> sirDeclarationParent(
    initialValue: T,
): PropertyDelegateProvider<SirDeclaration, ReadWriteProperty<SirDeclaration, T>> =
    PropertyDelegateProvider<SirDeclaration, ReadWriteProperty<SirDeclaration, T>> { thisRef, _ ->
        object : ReadWriteProperty<SirDeclaration, T> {

            private var value = initialValue

            init {
                value.declarations.add(thisRef)
            }

            override fun getValue(thisRef: SirDeclaration, property: KProperty<*>): T =
                value

            override fun setValue(thisRef: SirDeclaration, property: KProperty<*>, value: T) {
                this.value.declarations.remove(thisRef)

                this.value = value

                value.declarations.add(thisRef)
            }
        }
    }

fun sirEnumCaseParent(
    initialValue: SirEnumCase,
): PropertyDelegateProvider<SirEnumCaseAssociatedValue, ReadWriteProperty<SirEnumCaseAssociatedValue, SirEnumCase>> =
    PropertyDelegateProvider<SirEnumCaseAssociatedValue, ReadWriteProperty<SirEnumCaseAssociatedValue, SirEnumCase>> { thisRef, _ ->
        object : ReadWriteProperty<SirEnumCaseAssociatedValue, SirEnumCase> {

            private var value = initialValue

            init {
                value.associatedValues.add(thisRef)
            }

            override fun getValue(thisRef: SirEnumCaseAssociatedValue, property: KProperty<*>): SirEnumCase =
                value

            override fun setValue(thisRef: SirEnumCaseAssociatedValue, property: KProperty<*>, value: SirEnumCase) {
                this.value.associatedValues.remove(thisRef)

                this.value = value

                value.associatedValues.add(thisRef)
            }
        }
    }

fun sirTypeParameterParent(
    initialValue: SirTypeParameterParent,
): PropertyDelegateProvider<SirTypeParameter, ReadWriteProperty<SirTypeParameter, SirTypeParameterParent>> =
    PropertyDelegateProvider<SirTypeParameter, ReadWriteProperty<SirTypeParameter, SirTypeParameterParent>> { thisRef, _ ->
        object : ReadWriteProperty<SirTypeParameter, SirTypeParameterParent> {

            private var value = initialValue

            init {
                value.typeParameters.add(thisRef)
            }

            override fun getValue(thisRef: SirTypeParameter, property: KProperty<*>): SirTypeParameterParent =
                value

            override fun setValue(thisRef: SirTypeParameter, property: KProperty<*>, value: SirTypeParameterParent) {
                this.value.typeParameters.remove(thisRef)

                this.value = value

                value.typeParameters.add(thisRef)
            }
        }
    }

