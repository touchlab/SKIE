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
import io.outfoxx.swiftpoet.builder.BuilderWithAssociatedTypes
import io.outfoxx.swiftpoet.builder.BuilderWithConditionalConstraints
import io.outfoxx.swiftpoet.builder.BuilderWithDocs
import io.outfoxx.swiftpoet.builder.BuilderWithMembers
import io.outfoxx.swiftpoet.builder.BuilderWithModifiers
import io.outfoxx.swiftpoet.builder.BuilderWithSuperTypes
import io.outfoxx.swiftpoet.builder.BuilderWithTypeParameters
import io.outfoxx.swiftpoet.builder.BuilderWithTypeSpecs

/** A generated class, struct, enum or protocol declaration. */
class TypeSpec private constructor(
  builder: Builder,
) : AnyTypeSpec(builder.name, builder.attributes, builder.tags) {

  internal val kind = builder.kind
  val doc = builder.doc.build()
  val modifiers = kind.modifiers.toImmutableSet()
  val typeVariables = builder.typeVariables.toImmutableList()
  val conditionalConstraints = builder.conditionalConstraints.toImmutableList()
  val associatedTypes = builder.associatedTypes.toImmutableList()

  val isEnum = builder.isEnum

  val superTypes = builder.superTypes.toImmutableSet()
  val enumCases = builder.enumCases.toImmutableList()
  val propertySpecs = builder.propertySpecs.toImmutableList()
  val funSpecs = builder.functionSpecs.toImmutableList()
  override val typeSpecs = builder.typeSpecs.toImmutableList()

  fun toBuilder(): Builder {
    val builder = Builder(kind, name)
    builder.doc.add(doc)
    builder.attributes += attributes
    builder.typeVariables += typeVariables
    builder.conditionalConstraints += conditionalConstraints
    builder.superTypes += superTypes
    builder.enumCases += enumCases
    builder.propertySpecs += propertySpecs
    builder.functionSpecs += funSpecs
    builder.typeSpecs += typeSpecs
    builder.associatedTypes += associatedTypes
    return builder
  }

  override fun emit(codeWriter: CodeWriter) {
    // Nested classes interrupt wrapped line indentation. Stash the current wrapping state and put
    // it back afterwards when this type is complete.
    val previousStatementLine = codeWriter.statementLine
    codeWriter.statementLine = -1

    try {
      codeWriter.emitDoc(doc)
      codeWriter.emitAttributes(attributes)
      codeWriter.emitModifiers(kind.modifiers, setOf(INTERNAL))
      codeWriter.emit(kind.declarationKeyword)
      codeWriter.emitCode(" %L", escapeIfNecessary(name))
      codeWriter.emitTypeVariables(typeVariables)

      val superTypes =
        if (superTypes.contains(CLASS)) {
          listOf(CodeBlock.of("%T", CLASS)) + superTypes.filterNot { it == CLASS }.map { type -> CodeBlock.of("%T", type) }
        } else {
          superTypes.map { type -> CodeBlock.of("%T", type) }
        }

      if (superTypes.isNotEmpty()) {
        codeWriter.emitCode(superTypes.joinToCode(separator = ",%W", prefix = " : "))
      }

      codeWriter.emitWhereBlock(typeVariables + conditionalConstraints, forceOutput = conditionalConstraints.isNotEmpty())
      codeWriter.emit(" {\n\n")

      codeWriter.pushType(this)
      codeWriter.indent()

      if (associatedTypes.isNotEmpty()) {
        for (associatedType in associatedTypes) {
          codeWriter.emit("associatedtype ")
          associatedType.emit(codeWriter)
          if (associatedType.bounds.size > 1 || associatedType.bounds.any { it.constraint == TypeVariableName.Bound.Constraint.SAME_TYPE }) {
            codeWriter.emitWhereBlock(listOf(associatedType), forceOutput = true)
//             codeWriter.emit(" where")
//             associatedType.bounds.forEach {
//
//               associatedType.emit(codeWriter)
//               it.emit(codeWriter)
//             }
          } else {
            associatedType.bounds.forEach { it.emit(codeWriter) }
          }
          codeWriter.emit("\n")
        }

        codeWriter.emit("\n")
      }

      if (enumCases.isNotEmpty()) {
        for (enumCase in enumCases) {
          enumCase.emit(codeWriter)
          codeWriter.emit("\n")
        }

        codeWriter.emit("\n")
      }

      // Properties.
      for (propertySpec in propertySpecs) {
        propertySpec.emit(codeWriter, kind.implicitPropertyModifiers)
        codeWriter.emit("\n")
      }

      // Constructors.
      val constructors = funSpecs.filter { it.isConstructor }
      constructors.forEach { funSpec ->
        funSpec.emit(codeWriter, name, kind.implicitFunctionModifiers)
        codeWriter.emit("\n")
      }

      // Functions.
      val functions = funSpecs.filterNot { it.isConstructor }
      functions.forEach { funSpec ->
        funSpec.emit(codeWriter, name, kind.implicitFunctionModifiers)
        codeWriter.emit("\n")
      }

      // Types.
      typeSpecs.forEach { typeSpec ->
        typeSpec.emit(codeWriter)
        codeWriter.emit("\n")
      }

      codeWriter.unindent()
      codeWriter.popType()

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

  sealed class Kind(
    internal val declarationKeyword: String,
    internal val defaultImplicitPropertyModifiers: Set<Modifier>,
    internal val defaultImplicitFunctionModifiers: Set<Modifier>,
    internal val modifiers: Set<Modifier> = emptySet(),
  ) {

    internal val implicitPropertyModifiers get() = defaultImplicitPropertyModifiers

    internal val implicitFunctionModifiers get() = defaultImplicitFunctionModifiers

    abstract fun plusModifiers(vararg modifiers: Modifier): Kind

    override fun toString() = javaClass.simpleName.uppercase()

    class Class(vararg modifiers: Modifier) : Kind(
      "class",
      setOf(INTERNAL),
      setOf(INTERNAL),
      modifiers.toSet(),
    ) {

      override fun plusModifiers(vararg modifiers: Modifier) =
        Class(*(this.modifiers.toTypedArray() + modifiers))
    }

    class Struct(vararg modifiers: Modifier) : Kind(
      "struct",
      setOf(INTERNAL),
      setOf(INTERNAL),
      modifiers.toSet(),
    ) {

      override fun plusModifiers(vararg modifiers: Modifier) =
        Struct(*(this.modifiers.toTypedArray() + modifiers))
    }

    class Protocol(vararg modifiers: Modifier) : Kind(
      "protocol",
      setOf(INTERNAL),
      setOf(INTERNAL),
      modifiers.toSet(),
    ) {

      override fun plusModifiers(vararg modifiers: Modifier) =
        Protocol(*(this.modifiers.toTypedArray() + modifiers))
    }

    class Enum(vararg modifiers: Modifier) : Kind(
      "enum",
      setOf(INTERNAL),
      setOf(INTERNAL),
      modifiers.toSet(),
    ) {

      override fun plusModifiers(vararg modifiers: Modifier) =
        Enum(*(this.modifiers.toTypedArray() + modifiers))
    }

    class Actor(vararg modifiers: Modifier) : Kind(
      "actor",
      setOf(INTERNAL),
      setOf(INTERNAL),
      modifiers.toSet(),
    ) {

      override fun plusModifiers(vararg modifiers: Modifier) =
        Actor(*(this.modifiers.toTypedArray() + modifiers))
    }
  }

  class Builder(
    internal var kind: Kind,
    internal val name: String,
  ) : AttributedSpec.Builder<Builder>(), BuilderWithModifiers, BuilderWithAssociatedTypes<Builder>, BuilderWithTypeSpecs, BuilderWithMembers,
    BuilderWithDocs<Builder>, BuilderWithSuperTypes<Builder>, BuilderWithConditionalConstraints<Builder>
  {

    internal val doc = CodeBlock.builder()
    internal val typeVariables = mutableListOf<TypeVariableName>()
    internal val conditionalConstraints = mutableListOf<TypeVariableName>()
    internal val superTypes = mutableSetOf<TypeName>()
    internal val enumCases = mutableListOf<EnumerationCaseSpec>()
    internal val propertySpecs = mutableListOf<PropertySpec>()
    internal val functionSpecs = mutableListOf<FunctionSpec>()
    internal val typeSpecs = mutableListOf<AnyTypeSpec>()
    internal val associatedTypes = mutableListOf<TypeVariableName>()
    internal val isEnum get() = kind is Kind.Enum
    internal val isClass = kind is Kind.Class
    internal val isStruct = kind is Kind.Struct
    internal val isProtocol = kind is Kind.Protocol

    override fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    override fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    override fun addModifiers(vararg modifiers: Modifier) = apply {
      kind = kind.plusModifiers(*modifiers)
    }

    fun addTypeVariables(typeVariables: Iterable<TypeVariableName>) = apply {
      this.typeVariables += typeVariables
    }

    override fun addTypeVariable(typeVariable: TypeVariableName) = apply {
      typeVariables += typeVariable
    }

    override fun addConditionalConstraint(conditionalConstraint: TypeVariableName) = apply {
      this.conditionalConstraints += conditionalConstraints
    }

    override fun addConditionalConstraints(conditionalConstraints: Iterable<TypeVariableName>) = apply {
      this.conditionalConstraints += conditionalConstraints
    }

    fun constrainToClass() = apply {
      check(isProtocol) { "${this.name} is not a protocol" }
      this.superTypes.add(CLASS)
    }

    override fun addSuperTypes(superTypes: Iterable<TypeName>) = apply {
      this.superTypes += superTypes
    }

    override fun addSuperType(superType: TypeName) = apply {
      this.superTypes += superType
    }

    fun addEnumCase(enumerationCaseSpec: EnumerationCaseSpec) = apply {
      check(isEnum) { "${this.name} is not an enum" }
      require(enumCases.none { it.name == enumerationCaseSpec.name }) { "case already exists: ${enumerationCaseSpec.name}" }
      enumCases.add(enumerationCaseSpec)
    }

    fun addEnumCase(name: String, type: TupleTypeName) = apply {
      addEnumCase(EnumerationCaseSpec.builder(name, type).build())
    }

    fun addEnumCase(name: String, type: TypeName) = apply {
      addEnumCase(EnumerationCaseSpec.builder(name, type).build())
    }

    fun addEnumCase(name: String, constant: String) = apply {
      addEnumCase(EnumerationCaseSpec.builder(name, constant).build())
    }

    fun addEnumCase(name: String, constant: Int) = apply {
      addEnumCase(EnumerationCaseSpec.builder(name, constant).build())
    }

    fun addEnumCase(name: String, constant: CodeBlock) = apply {
      addEnumCase(EnumerationCaseSpec.builder(name, constant).build())
    }

    fun addEnumCase(name: String) = apply {
      addEnumCase(EnumerationCaseSpec.builder(name).build())
    }

    fun addProperties(propertySpecs: Iterable<PropertySpec>) = apply {
      propertySpecs.map(this::addProperty)
    }

    override fun addProperty(propertySpec: PropertySpec) = apply {
      propertySpecs += propertySpec
    }

    fun addProperty(name: String, type: TypeName, vararg modifiers: Modifier) =
      addProperty(PropertySpec.builder(name, type, *modifiers).build())

    fun addMutableProperty(name: String, type: TypeName, vararg modifiers: Modifier) =
      addProperty(PropertySpec.varBuilder(name, type, *modifiers).build())

    fun addFunctions(functionSpecs: Iterable<FunctionSpec>) = apply {
      functionSpecs.forEach { addFunction(it) }
    }

    override fun addFunction(functionSpec: FunctionSpec) = apply {
      check(!isProtocol || functionSpec.body === CodeBlock.ABSTRACT) { "Protocols require abstract functions; see FunctionSpec.abstractBuilder(...)" }
      requireNoneOrOneOf(functionSpec.modifiers, Modifier.OPEN, INTERNAL, Modifier.PUBLIC, Modifier.PRIVATE)
      functionSpecs += functionSpec
    }

    fun addTypes(typeSpecs: Iterable<AnyTypeSpec>) = apply {
      typeSpecs.forEach(::addType)
    }

    override fun addType(typeSpec: AnyTypeSpec) = apply {
      check(!isProtocol || typeSpec is TypeAliasSpec) {
        "${this.name} is a protocol, it can only contain type aliases as nested types"
      }
      check(!(typeSpec is TypeSpec && typeSpec.kind is Kind.Protocol)) {
        "${typeSpec.name} is a protocol, it cannot be added as a nested type"
      }
      typeSpecs += typeSpec
    }

    override fun addAssociatedType(typeVariable: TypeVariableName) = apply {
      check(isProtocol) { "${this.name} is not a protocol, only protocols can have associated types" }
      associatedTypes += typeVariable
    }

    fun build(): TypeSpec {
      return TypeSpec(this)
    }
  }

  companion object {

    @JvmStatic
    fun classBuilder(name: String) = Builder(Kind.Class(), name)

    @JvmStatic
    fun classBuilder(className: DeclaredTypeName) = classBuilder(className.simpleName)

    @JvmStatic
    fun structBuilder(name: String) = Builder(Kind.Struct(), name)

    @JvmStatic
    fun structBuilder(structName: DeclaredTypeName) = structBuilder(structName.simpleName)

    @JvmStatic
    fun protocolBuilder(name: String) = Builder(Kind.Protocol(), name)

    @JvmStatic
    fun protocolBuilder(protocolName: DeclaredTypeName) = protocolBuilder(protocolName.simpleName)

    @JvmStatic
    fun enumBuilder(name: String) = Builder(Kind.Enum(), name)

    @JvmStatic
    fun enumBuilder(enumName: DeclaredTypeName) = enumBuilder(enumName.simpleName)

    @JvmStatic
    fun actorBuilder(name: String) = Builder(Kind.Actor(), name)

    @JvmStatic
    fun actorBuilder(actorName: DeclaredTypeName) = actorBuilder(actorName.simpleName)
  }
}

private object CLASS : TypeName() {

  override fun emit(out: CodeWriter): CodeWriter {
    return out.emit("class")
  }
}
