package co.touchlab.skie.kotlingenerator

import co.touchlab.skie.kotlingenerator.ir.KotlinClass
import co.touchlab.skie.kotlingenerator.ir.KotlinDeclaration
import co.touchlab.skie.kotlingenerator.ir.KotlinFile
import co.touchlab.skie.kotlingenerator.ir.KotlinFunction
import co.touchlab.skie.kotlingenerator.ir.KotlinProperty
import co.touchlab.skie.kotlingenerator.ir.KotlinType
import co.touchlab.skie.kotlingenerator.ir.KotlinTypeParameter

object KotlinGenerator {

    fun generate(kotlinFile: KotlinFile): String {
        val context = Context(kotlinFile)

        val content = SmartStringBuilder {
            with(context) {
                append(kotlinFile)
            }
        }

        return SmartStringBuilder {
            +"package ${kotlinFile.packageName}"

            with(context) {
                appendImports()
            }

            append(content)
        }
    }

    context(Context)
    private fun SmartStringBuilder.append(file: KotlinFile) {
        file.declarations.forEach {
            append(it)
        }
    }

    context(Context)
    private fun SmartStringBuilder.append(declaration: KotlinDeclaration) {
        +""

        when (declaration) {
            is KotlinClass -> append(declaration)
            is KotlinFunction -> append(declaration)
            is KotlinProperty -> append(declaration)
        }
    }

    context(Context)
    private fun SmartStringBuilder.append(clazz: KotlinClass) {
        when (clazz.kind) {
            KotlinClass.Kind.Class -> append("class ")
            KotlinClass.Kind.Interface -> append("interface ")
        }

        append(clazz.name)

        appendTypeParameters(clazz)

        +" {"

        indented {
            clazz.declarations.forEach {
                append(it)
            }
        }

        +"}"
    }

    context(Context)
    private fun SmartStringBuilder.appendTypeParameters(clazz: KotlinClass) {
        when (clazz.typeParameters.size) {
            0 -> {}
            1 -> {
                val typeParameter = clazz.typeParameters.single()

                typeParameter.bounds.forEach { registerType(it) }

                append("<${typeParameter.name}")
                appendSingleBound(typeParameter)
                append(">")
            }
            else -> {
                +"<"

                indented {
                    clazz.typeParameters.forEach { typeParameter ->
                        typeParameter.bounds.forEach { registerType(it) }

                        append(typeParameter.name)
                        appendSingleBound(typeParameter)
                        +","
                    }
                }

                append(">")
            }
        }

        appendWhere(clazz)
    }

    context(Context)
    private fun SmartStringBuilder.appendSingleBound(typeParameter: KotlinTypeParameter) {
        if (typeParameter.bounds.size == 1) {
            append(" : ${typeParameter.bounds.single().toKotlinName()}")
        }
    }

    context(Context)
    private fun SmartStringBuilder.appendWhere(clazz: KotlinClass) {
        val typeParametersWithBounds = clazz.typeParameters
            .filter { it.bounds.size > 1 }
            .flatMap { typeParameter -> typeParameter.bounds.map { typeParameter to it } }

        if (typeParametersWithBounds.isNotEmpty()) {
            +" where"
        }

        typeParametersWithBounds.forEachIndexed { index, (typeParameter, bound) ->
            if (index != 0) {
                +","
            }

            append("    ${typeParameter.name} : ${bound.toKotlinName()}")
        }
    }

    context(Context)
    private fun SmartStringBuilder.append(function: KotlinFunction) {
        function.extensionReceiver?.let { registerType(it) }
        function.valueParameters.forEach {
            registerType(it.type)
        }
        registerType(function.returnType)

        function.annotations.forEach {
            +it
        }

        val suspendModifier = "suspend ".takeIf { function.isSuspend } ?: ""
        val extensionReceiver = function.extensionReceiver?.toKotlinName()?.let { "$it." } ?: ""
        val valueParameters = function.valueParameters.joinToString(", ") { "${it.name}: ${it.type.toKotlinName()}" }

        +"${suspendModifier}fun ${extensionReceiver}${function.name}($valueParameters): ${function.returnType.toKotlinName()} {"
        indented {
            +function.body
        }
        +"}"
    }

    context(Context)
    private fun SmartStringBuilder.append(property: KotlinProperty) {
        registerType(property.type)

        +"val ${property.name}: ${property.type.toKotlinName()} = ${property.initializer}"
    }

    private fun KotlinType.toKotlinName(): String =
        when (this) {
            is KotlinType.Declared -> this.name
            is KotlinType.Lambda -> {
                val suspendModifier = "suspend ".takeIf { this.isSuspend } ?: ""
                val receiver = this.receiverType?.toKotlinName()?.let { "$it." } ?: ""
                val valueParameters = this.parameterTypes.joinToString(", ") { it.toKotlinName() }
                val returnType = this.returnType.toKotlinName()

                "($suspendModifier$receiver($valueParameters) -> $returnType)"
            }
            is KotlinType.Optional -> this.wrapped.toKotlinName() + "?"
            is KotlinType.Parametrized -> {
                this.kotlinType.toKotlinName() + "<" + this.typeArguments.joinToString(", ") { it.toKotlinName() } + ">"
            }
            KotlinType.Star -> "*"
            is KotlinType.TypeParameterUsage -> this.typeParameter.name
        }

    private class Context(
        private val file: KotlinFile,
    ) {

        private val imports = mutableSetOf<KotlinType.Declared>()

        fun SmartStringBuilder.appendImports() {
            if (imports.isNotEmpty()) {
                +""
            }

            imports
                .filterNot { it.packageName == "kotlin" }
                .filterNot { it.packageName == file.packageName }
                .map { "import ${it.packageName}.${it.name.substringBefore(".")}" }
                .distinct()
                .sorted()
                .forEach {
                    +it
                }
        }

        fun registerType(type: KotlinType) {
            fun registerTypeRecursively(type: KotlinType, visitedTypeParameters: List<KotlinTypeParameter>) {
                when (type) {
                    is KotlinType.Declared -> imports.add(type)
                    is KotlinType.Lambda -> {
                        type.parameterTypes.forEach { registerTypeRecursively(it, visitedTypeParameters) }
                        type.receiverType?.let { registerTypeRecursively(it, visitedTypeParameters) }
                        registerTypeRecursively(type.returnType, visitedTypeParameters)
                    }
                    is KotlinType.Optional -> registerTypeRecursively(type.wrapped, visitedTypeParameters)
                    is KotlinType.Parametrized -> {
                        registerTypeRecursively(type.kotlinType, visitedTypeParameters)
                        type.typeArguments.forEach { registerTypeRecursively(it, visitedTypeParameters) }
                    }
                    KotlinType.Star -> {}
                    is KotlinType.TypeParameterUsage -> {
                        if (type.typeParameter !in visitedTypeParameters) {
                            type.typeParameter.bounds.forEach {
                                registerTypeRecursively(it, visitedTypeParameters + type.typeParameter)
                            }
                        }
                    }
                }
            }

            registerTypeRecursively(type, emptyList())
        }
    }
}
