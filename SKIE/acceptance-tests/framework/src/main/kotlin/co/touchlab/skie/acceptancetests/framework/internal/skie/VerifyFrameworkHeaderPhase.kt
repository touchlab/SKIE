package co.touchlab.skie.acceptancetests.framework.internal.skie

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.swift.SirCodeGenerator
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConditionalConstraintParent
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclarationNamespace
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirOverridableDeclaration
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.isAccessibleFromOtherModules
import co.touchlab.skie.sir.element.isOverriddenFromReadOnlyProperty
import co.touchlab.skie.sir.element.module
import co.touchlab.skie.sir.element.shallowCopy
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import co.touchlab.skie.util.swift.findFirstSkieErrorType
import kotlin.io.path.Path
import kotlin.io.path.writeText

object VerifyFrameworkHeaderPhase : SirPhase {

    private const val className = "HeaderVerification"

    context(SirPhase.Context)
    override fun isActive(): Boolean = globalConfiguration[TestConfigurationKeys.EnableVerifyFrameworkHeaderPhase]

    context(SirPhase.Context)
    override suspend fun execute() {
        val headerValidationSwiftFilePath = globalConfiguration[TestConfigurationKeys.VerifyFrameworkHeaderPhaseSwiftFilePath]

        val fileContent = generateFileContent()

        headerValidationSwiftFilePath.writeText(fileContent)
    }

    context(SirPhase.Context)
    private fun generateFileContent(): String {
        // Not used outside of this file; therefore it's intentionally not included in SirProvider
        val sirModule = SirModule.Skie(className)

        val sirFile = SirIrFile(sirModule, Path("$className.swift"))

        sirFile.imports.add(framework.frameworkName)

        sirFile.addVerificationFileBody()

        return SirCodeGenerator.generate(sirFile)
    }

    context(SirPhase.Context)
    private fun SirIrFile.addVerificationFileBody() {
        SirClass(
            baseName = className,
            kind = SirClass.Kind.Struct,
        ).apply {
            addVerificationClassBody()
        }
    }

    context(SirPhase.Context, SirClass)
    private fun addVerificationClassBody() {
        sirProvider.allLocalCallableDeclarations
            .filter { it.visibility.isAccessibleFromOtherModules }
            .filter { it.findFirstSkieErrorType() == null }
            .forEach {
                addCallableDeclarationVerification(it)
            }
    }

    context(SirClass, SirPhase.Context)
    private fun addCallableDeclarationVerification(callableDeclaration: SirCallableDeclaration) {
        when (callableDeclaration) {
            is SirConstructor -> addConstructorVerification(callableDeclaration)
            is SirSimpleFunction -> addSimpleFunctionVerification(callableDeclaration)
            is SirProperty -> addPropertyVerification(callableDeclaration)
        }
    }

    context(SirClass)
    private fun addConstructorVerification(constructor: SirConstructor) {
        SirSimpleFunction(
            identifier = constructor.getVerificationFunctionIdentifier(),
            returnType = constructor.returnType,
            deprecationLevel = constructor.deprecationLevel,
            throws = constructor.throws,
            attributes = constructor.attributes,
            modifiers = constructor.modifiers,
        ).apply {
            copyTypeParametersFrom(constructor.parent.classDeclaration)

            applyTypeParameterConstraintsFrom(constructor.parent)

            copyValueParametersFrom(constructor)

            bodyBuilder.add {
                addStatement(
                    "return %L%N.%L",
                    if (constructor.throws) "try " else "",
                    constructor.parent.classDeclaration.publicName.toString(),
                    constructor.call(valueParameters.map { it.name.escapeSwiftIdentifier() }),
                )
            }
        }
    }

    context(SirClass)
    private fun addSimpleFunctionVerification(function: SirSimpleFunction) {
        function.shallowCopy(
            identifier = function.getVerificationFunctionIdentifier(),
            parent = this@SirClass,
            visibility = SirVisibility.Public,
            isReplaced = false,
            isHidden = false,
            scope = SirScope.Member,
            isFakeOverride = false,
            isAbstract = false,
        ).apply {
            function.memberOwner?.let { addReceiverPassthrough(it) }

            val typeParametersDeclaredByFunction = function.typeParameters.filter { it.parent == function }
            copyTypeParametersFrom(typeParametersDeclaredByFunction, function.typeParameters)

            applyTypeParameterConstraintsFrom(function.parent)

            copyValueParametersFrom(function)

            val valueParametersForFunctionCall = if (function.memberOwner != null) valueParameters.drop(1) else valueParameters

            bodyBuilder.add {
                addStatement(
                    "return %L%L%N.%L",
                    if (function.throws) "try " else "",
                    if (function.isAsync) "await " else "",
                    getReceiverValue(function),
                    function.call(valueParametersForFunctionCall.map { it.name.escapeSwiftIdentifier() }),
                )
            }
        }
    }

    context(SirClass, SirPhase.Context)
    private fun addPropertyVerification(property: SirProperty) {
        property.getter?.let {
            addPropertyGetterVerification(it)
        }

        // TODO Remove this filter once SKIE generates custom header
        property.setter?.takeIf { !it.property.isOverriddenFromReadOnlyProperty }?.let {
            addPropertySetterVerification(it)
        }
    }

    context(SirClass)
    private fun addPropertyGetterVerification(getter: SirGetter) {
        val property = getter.property

        SirSimpleFunction(
            identifier = property.getVerificationFunctionIdentifier(),
            returnType = property.type,
            deprecationLevel = property.deprecationLevel,
            throws = getter.throws,
            attributes = getter.attributes + property.attributes,
            modifiers = property.modifiers,
        ).apply {
            property.memberOwner?.let { addReceiverPassthrough(it) }

            applyTypeParameterConstraintsFrom(property.parent)

            bodyBuilder.add {
                addStatement(
                    "return %L%N.%N",
                    if (getter.throws) "try " else "",
                    getReceiverValue(property),
                    property.reference,
                )
            }
        }
    }

    context(SirClass, SirPhase.Context)
    private fun addPropertySetterVerification(setter: SirSetter) {
        val property = setter.property

        SirSimpleFunction(
            identifier = property.getVerificationFunctionIdentifier(),
            returnType = sirBuiltins.Swift.Void.defaultType,
            deprecationLevel = property.deprecationLevel,
            throws = setter.throws,
            attributes = setter.attributes + property.attributes,
            modifiers = setter.modifiers + property.modifiers,
        ).apply {
            property.memberOwner?.let { addReceiverPassthrough(it) }

            applyTypeParameterConstraintsFrom(property.parent)

            SirValueParameter(
                "value",
                property.type,
            )

            bodyBuilder.add {
                addStatement(
                    "%L%N.%N = %N",
                    if (setter.throws) "try " else "",
                    getReceiverValue(property),
                    property.reference,
                    "value",
                )
            }
        }
    }

    private fun getReceiverValue(overridableDeclaration: SirOverridableDeclaration<*>): String =
        when (overridableDeclaration.scope) {
            SirScope.Member -> "receiver"
            SirScope.Static, SirScope.Class -> overridableDeclaration.memberOwner!!.publicName.toString()
            SirScope.Global -> overridableDeclaration.module.name
        }

    private fun SirSimpleFunction.addReceiverPassthrough(sirClass: SirClass) {
        copyTypeParametersFrom(sirClass)

        val typeArguments = typeParameters.map { it.toTypeParameterUsage() }

        SirValueParameter(
            name = "receiver",
            type = sirClass.toType(typeArguments),
        )
    }

    private fun SirSimpleFunction.applyTypeParameterConstraintsFrom(parent: SirDeclarationParent) {
        val extension = parent as? SirConditionalConstraintParent ?: return

        extension.conditionalConstraints.forEach { constraint ->
            val typeParameter = typeParameters.firstOrNull { it.name == constraint.typeParameter.name }

            typeParameter?.bounds?.addAll(constraint.bounds)
        }
    }

    context(SirClass)
    private fun nextIndex(): Int = declarations.size

    context(SirClass)
    private fun SirCallableDeclaration.getVerificationFunctionIdentifier(): String =
        (((this.parent as? SirDeclarationNamespace)?.fqName?.toLocalString()?.let { "${it}__" } ?: "") +
            this.identifierAfterVisibilityChange +
            "__${nextIndex()}").replace(".", "__")
}
