package co.touchlab.skie.sir.element.util

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraint
import co.touchlab.skie.sir.element.SirConditionalConstraintParent
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirEnumCaseAssociatedValue
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirPropertyAccessor
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirTypeParameterParent
import co.touchlab.skie.sir.element.SirValueParameter
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T : SirDeclarationParent> sirDeclarationParent(initialValue: T) = parent<SirDeclaration, T>(
    initialValue = initialValue,
    onChange = { oldValue, newValue, thisRef ->
        oldValue?.declarations?.remove(thisRef)

        newValue.declarations.add(thisRef)
    },
)

fun sirEnumCaseParent(initialValue: SirClass) = parent<SirEnumCase, SirClass>(
    initialValue = initialValue,
    onChange = { oldValue, newValue, thisRef ->
        oldValue?.enumCases?.remove(thisRef)

        newValue.enumCases.add(thisRef)
    },
)

fun sirEnumCaseAssociatedValueParent(initialValue: SirEnumCase) = parent<SirEnumCaseAssociatedValue, SirEnumCase>(
    initialValue = initialValue,
    onChange = { oldValue, newValue, thisRef ->
        oldValue?.associatedValues?.remove(thisRef)

        newValue.associatedValues.add(thisRef)
    },
)

fun sirTypeParameterParent(initialValue: SirTypeParameterParent) = parent<SirTypeParameter, SirTypeParameterParent>(
    initialValue = initialValue,
    onChange = { oldValue, newValue, thisRef ->
        oldValue?.typeParameters?.remove(thisRef)

        newValue.typeParameters.add(thisRef)
    },
)

fun sirValueParameterParent(initialValue: SirFunction) = parent<SirValueParameter, SirFunction>(
    initialValue = initialValue,
    onChange = { oldValue, newValue, thisRef ->
        oldValue?.valueParameters?.remove(thisRef)

        newValue.valueParameters.add(thisRef)
    },
)

fun <T : SirPropertyAccessor> sirPropertyAccessorParent(initialValue: SirProperty) = parent<T, SirProperty>(
    initialValue = initialValue,
    onChange = { _, newValue, thisRef ->
        when (thisRef) {
            is SirGetter -> newValue.setGetterInternal(thisRef)
            is SirSetter -> newValue.setSetterInternal(thisRef)
        }
    },
)

fun sirConditionalConstraintParent(initialValue: SirConditionalConstraintParent) =
    parent<SirConditionalConstraint, SirConditionalConstraintParent>(
        initialValue = initialValue,
        onChange = { oldValue, newValue, thisRef ->
            oldValue?.conditionalConstraints?.remove(thisRef)

            newValue.conditionalConstraints.add(thisRef)
        },
    )

private fun <CHILD : Any, PARENT : Any> parent(
    initialValue: PARENT,
    onChange: (oldValue: PARENT?, newValue: PARENT, thisRef: CHILD) -> Unit,
): PropertyDelegateProvider<CHILD, ReadWriteProperty<CHILD, PARENT>> =
    PropertyDelegateProvider<CHILD, ReadWriteProperty<CHILD, PARENT>> { thisRef, _ ->
        object : ReadWriteProperty<CHILD, PARENT> {

            private var value = initialValue

            init {
                onChange(null, value, thisRef)
            }

            override fun getValue(thisRef: CHILD, property: KProperty<*>): PARENT = value

            override fun setValue(thisRef: CHILD, property: KProperty<*>, value: PARENT) {
                val oldValue = this.value

                this.value = value

                onChange(oldValue, value, thisRef)
            }
        }
    }
