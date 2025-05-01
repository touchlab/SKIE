/*
 * Copyright 2018 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.swiftpoet

import io.outfoxx.swiftpoet.Modifier.INTERNAL
import io.outfoxx.swiftpoet.builder.BuilderWithConditionalConstraints
import io.outfoxx.swiftpoet.builder.BuilderWithDocs
import io.outfoxx.swiftpoet.builder.BuilderWithMembers
import io.outfoxx.swiftpoet.builder.BuilderWithModifiers
import io.outfoxx.swiftpoet.builder.BuilderWithSuperTypes
import io.outfoxx.swiftpoet.builder.BuilderWithTypeSpecs

/** A generated class, protocol, or enum declaration.  */
class ExtensionSpec private constructor(builder: Builder) : AttributedSpec(builder.attributes.toImmutableList(), builder.tags) {

    val doc = builder.doc.build()
    val extendedTypeOrName = builder.extendedTypeOrName
    val modifiers = builder.modifiers.toImmutableSet()
    val superTypes = builder.superTypes.toImmutableSet()
    val conditionalConstraints = builder.conditionalConstraints.toImmutableList()
    val propertySpecs = builder.propertySpecs.toImmutableList()
    val funSpecs = builder.functionSpecs.toImmutableList()
    val typeSpecs = builder.typeSpecs.toImmutableList()

    fun toBuilder(): Builder {
        val builder = Builder(extendedTypeOrName)
        builder.doc.add(doc)
        builder.attributes += attributes
        builder.conditionalConstraints += conditionalConstraints
        builder.propertySpecs += propertySpecs
        builder.functionSpecs += funSpecs
        builder.typeSpecs += typeSpecs
        return builder
    }

    internal fun emit(codeWriter: CodeWriter) {
        // Nested classes interrupt wrapped line indentation. Stash the current wrapping state and put
        // it back afterwards when this type is complete.
        val previousStatementLine = codeWriter.statementLine
        codeWriter.statementLine = -1

        try {
            codeWriter.emitDoc(doc)
            codeWriter.emitAttributes(attributes)
            codeWriter.emitModifiers(modifiers, setOf(INTERNAL))
            codeWriter.emit("extension")
            codeWriter.emitCode(" %T", extendedTypeOrName)

            val superTypes = superTypes.map { type -> CodeBlock.of("%T", type) }

            if (superTypes.isNotEmpty()) {
                codeWriter.emitCode(superTypes.joinToCode(separator = ",%W", prefix = " : "))
            }

            codeWriter.emitWhereBlock(conditionalConstraints, true)
            codeWriter.emit(" {\n\n")

            val typeName =
                if (extendedTypeOrName is AnyTypeSpec) {
                    codeWriter.pushModule(codeWriter.currentModule)
                    extendedTypeOrName.name
                } else {
                    extendedTypeOrName as DeclaredTypeName
                    codeWriter.pushModule(extendedTypeOrName.moduleName)
                    extendedTypeOrName.simpleName
                }
            codeWriter.pushType(ExternalTypeSpec(typeName))

            codeWriter.indent()

            // Types.
            for (typeSpec in typeSpecs) {
                typeSpec.emit(codeWriter)
                codeWriter.emit("\n")
            }

            // Properties.
            for (propertySpec in propertySpecs) {
                propertySpec.emit(codeWriter, setOf(INTERNAL))
                codeWriter.emit("\n")
            }

            // Constructors.
            for (funSpec in funSpecs) {
                if (!funSpec.isConstructor) continue
                funSpec.emit(codeWriter, typeName, setOf(INTERNAL))
                codeWriter.emit("\n")
            }

            // Functions.
            for (funSpec in funSpecs) {
                if (funSpec.isConstructor) continue
                funSpec.emit(codeWriter, typeName, setOf(INTERNAL))
                codeWriter.emit("\n")
            }

            codeWriter.unindent()
            codeWriter.popType()
            codeWriter.popModule()

            codeWriter.emit("}\n")
        } finally {
            codeWriter.statementLine = previousStatementLine
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        return toString() == other.toString()
    }

    override fun hashCode() = toString().hashCode()

    override fun toString() = buildString { emit(CodeWriter(this)) }

    class Builder internal constructor(internal val extendedTypeOrName: Any) :
        AttributedSpec.Builder<Builder>(),
        BuilderWithModifiers,
        BuilderWithTypeSpecs,
        BuilderWithMembers,
        BuilderWithDocs<Builder>,
        BuilderWithConditionalConstraints<Builder>,
        BuilderWithSuperTypes<Builder> {

        internal val doc = CodeBlock.builder()
        internal val modifiers = mutableSetOf<Modifier>()
        internal val superTypes = mutableListOf<TypeName>()
        internal val conditionalConstraints = mutableListOf<TypeVariableName>()
        internal val propertySpecs = mutableListOf<PropertySpec>()
        internal val functionSpecs = mutableListOf<FunctionSpec>()
        internal val typeSpecs = mutableListOf<AnyTypeSpec>()

        override fun addDoc(format: String, vararg args: Any) = apply {
            doc.add(format, *args)
        }

        override fun addDoc(block: CodeBlock) = apply {
            doc.add(block)
        }

        override fun addModifiers(vararg modifiers: Modifier) = apply {
            this.modifiers += modifiers
        }

        override fun addSuperTypes(superTypes: Iterable<TypeName>) = apply {
            this.superTypes += superTypes
        }

        override fun addSuperType(superType: TypeName) = apply {
            superTypes += superType
        }

        override fun addConditionalConstraints(conditionalConstraints: Iterable<TypeVariableName>) = apply {
            this.conditionalConstraints += conditionalConstraints
        }

        override fun addConditionalConstraint(conditionalConstraint: TypeVariableName) = apply {
            conditionalConstraints += conditionalConstraint
        }

        fun addProperties(propertySpecs: Iterable<PropertySpec>) = apply {
            propertySpecs.map(this::addProperty)
        }

        override fun addProperty(propertySpec: PropertySpec) = apply {
            propertySpecs += propertySpec
        }

        fun addProperty(name: String, type: TypeName, vararg modifiers: Modifier) =
            addProperty(PropertySpec.builder(name, type, *modifiers).build())

        fun addFunctions(functionSpecs: Iterable<FunctionSpec>) = apply {
            functionSpecs.forEach { addFunction(it) }
        }

        override fun addFunction(functionSpec: FunctionSpec) = apply {
            requireNoneOrOneOf(functionSpec.modifiers, Modifier.OPEN, INTERNAL, Modifier.PUBLIC, Modifier.PRIVATE)
            functionSpecs += functionSpec
        }

        fun addTypes(typeSpecs: Iterable<AnyTypeSpec>) = apply {
            this.typeSpecs += typeSpecs
        }

        override fun addType(typeSpec: AnyTypeSpec) = apply {
            typeSpecs += typeSpec
        }

        fun build(): ExtensionSpec = ExtensionSpec(this)
    }

    companion object {

        @JvmStatic
        fun builder(extendedType: AnyTypeSpec) = Builder(extendedType)

        @JvmStatic
        fun builder(extendedType: DeclaredTypeName) = Builder(extendedType)
    }
}
