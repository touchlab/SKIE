package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel

class SkieClassSuspendGenerator {

    private val skieClassCache = mutableMapOf<KotlinTypeSwiftModel, SirClass>()

    context(SwiftModelScope)
    fun getOrCreateSkieClass(swiftModel: KotlinTypeSwiftModel): SirClass =
        skieClassCache.getOrPut(swiftModel) {
            val skieClass = createSkieClass(swiftModel)

            generateNamespaceProvider(swiftModel, skieClass)

            skieClass
        }

    context(SwiftModelScope)
    private fun createSkieClass(swiftModel: KotlinTypeSwiftModel): SirClass =
        SirClass(
            baseName = "Suspend",
            parent = sirProvider.getSkieNamespace(swiftModel),
            visibility = SirVisibility.PublicButReplaced,
        ).apply {
            copyTypeParametersFrom(swiftModel.kotlinSirClass)

            addSkieClassMembers(swiftModel)
        }

    context(SwiftModelScope)
    private fun generateNamespaceProvider(swiftModel: KotlinTypeSwiftModel, skieClass: SirClass) {
        SirFunction(
            identifier = "skie",
            parent = sirProvider.getFile(swiftModel),
            returnType = sirBuiltins.Swift.Void.defaultType,
        ).apply {
            copyTypeParametersFrom(skieClass)

            SirValueParameter(
                label = "_",
                name = "kotlinObject",
                type = swiftModel.kotlinSirClass.toTypeFromEnclosingTypeParameters(typeParameters),
            )

            returnType = skieClass.toTypeFromEnclosingTypeParameters(typeParameters)

            swiftPoetBuilderModifications.add {
                addCode("return %T(kotlinObject)", skieClass.defaultType.toSwiftPoetDeclaredTypeName())
            }
        }
    }

    companion object {

        const val kotlinObjectVariableName: String = "__kotlinObject"
    }
}

private fun SirClass.addSkieClassMembers(
    swiftModel: KotlinTypeSwiftModel,
) {
    addSkieClassKotlinObjectHolder(swiftModel)

    addSkieClassConstructor(swiftModel)
}

private fun SirClass.addSkieClassKotlinObjectHolder(swiftModel: KotlinTypeSwiftModel) {
    SirProperty(
        name = SkieClassSuspendGenerator.kotlinObjectVariableName,
        type = swiftModel.kotlinSirClass.toTypeFromEnclosingTypeParameters(typeParameters),
    )
}

private fun SirClass.addSkieClassConstructor(swiftModel: KotlinTypeSwiftModel) {
    SirConstructor().apply {
        SirValueParameter(
            label = "_",
            name = SkieClassSuspendGenerator.kotlinObjectVariableName,
            type = swiftModel.kotlinSirClass.toTypeFromEnclosingTypeParameters(typeParameters),
        )

        swiftPoetBuilderModifications.add {
            addCode("self.${SkieClassSuspendGenerator.kotlinObjectVariableName} = ${SkieClassSuspendGenerator.kotlinObjectVariableName}")
        }
    }
}
