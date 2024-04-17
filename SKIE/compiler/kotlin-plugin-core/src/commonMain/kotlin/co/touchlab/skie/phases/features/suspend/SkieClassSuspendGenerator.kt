package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters

class SkieClassSuspendGenerator {

    private val skieClassCache = mutableMapOf<KirClass, SirClass>()

    context(SirPhase.Context)
    fun getOrCreateSuspendClass(suspendFunctionOwner: KirClass): SirClass =
        skieClassCache.getOrPut(suspendFunctionOwner) {
            val skieClass = createSkieClass(suspendFunctionOwner)

            generateNamespaceProvider(suspendFunctionOwner, skieClass)

            skieClass
        }

    context(SirPhase.Context)
    private fun createSkieClass(suspendFunctionOwner: KirClass): SirClass =
        SirClass(
            baseName = "Suspend",
            parent = namespaceProvider.getNamespaceExtension(suspendFunctionOwner),
            visibility = SirVisibility.PublicButReplaced,
            kind = SirClass.Kind.Struct,
        ).apply {
            copyTypeParametersFrom(suspendFunctionOwner.originalSirClass)

            addSkieClassMembers(suspendFunctionOwner)
        }

    context(SirPhase.Context)
    private fun generateNamespaceProvider(suspendFunctionOwner: KirClass, skieClass: SirClass) {
        SirSimpleFunction(
            identifier = "skie",
            parent = namespaceProvider.getNamespaceFile(suspendFunctionOwner),
            returnType = sirBuiltins.Swift.Void.defaultType,
        ).apply {
            copyTypeParametersFrom(skieClass)

            SirValueParameter(
                label = "_",
                name = "kotlinObject",
                type = suspendFunctionOwner.originalSirClass.toTypeFromEnclosingTypeParameters(typeParameters),
            )

            returnType = skieClass.toTypeFromEnclosingTypeParameters(typeParameters)

            bodyBuilder.add {
                addCode("return %T(kotlinObject)", skieClass.defaultType.toSwiftPoetDeclaredTypeName())
            }
        }
    }

    companion object {

        const val kotlinObjectVariableName: String = "__kotlinObject"
    }
}

private fun SirClass.addSkieClassMembers(
    suspendFunctionOwner: KirClass,
) {
    addSkieClassKotlinObjectHolder(suspendFunctionOwner)

    addSkieClassConstructor(suspendFunctionOwner)
}

private fun SirClass.addSkieClassKotlinObjectHolder(suspendFunctionOwner: KirClass) {
    SirProperty(
        identifier = SkieClassSuspendGenerator.kotlinObjectVariableName,
        type = suspendFunctionOwner.originalSirClass.toTypeFromEnclosingTypeParameters(typeParameters),
    )
}

private fun SirClass.addSkieClassConstructor(suspendFunctionOwner: KirClass) {
    SirConstructor().apply {
        SirValueParameter(
            label = "_",
            name = SkieClassSuspendGenerator.kotlinObjectVariableName,
            type = suspendFunctionOwner.originalSirClass.toTypeFromEnclosingTypeParameters(typeParameters),
        )

        bodyBuilder.add {
            addCode("self.${SkieClassSuspendGenerator.kotlinObjectVariableName} = ${SkieClassSuspendGenerator.kotlinObjectVariableName}")
        }
    }
}
