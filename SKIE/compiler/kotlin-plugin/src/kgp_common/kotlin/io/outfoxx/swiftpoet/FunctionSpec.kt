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

/** A generated function declaration.  */
class FunctionSpec private constructor(
  builder: Builder,
) : AttributedSpec(builder.attributes.toImmutableList(), builder.tags) {

  val name = builder.name
  val doc = builder.doc.build()
  val modifiers = builder.modifiers.toImmutableSet()
  val typeVariables = builder.typeVariables.toImmutableList()
  val returnType = builder.returnType
  val parameters = builder.parameters.toImmutableList()
  val throws = builder.throws
  val async = builder.async
  val failable = builder.failable
  val localTypeSpecs = builder.localTypeSpecs
  val body = if (builder.abstract) CodeBlock.ABSTRACT else builder.body.build()
  val type = builder.type

  init {
    require(type != Type.Setter || parameters.size <= 1) {
      "$name must have zero or one parameter"
    }
  }

  internal fun emit(
    codeWriter: CodeWriter,
    enclosingName: String?,
    implicitModifiers: Set<Modifier>,
    conciseGetter: Boolean = false,
  ) {
    if (type == Type.Getter && conciseGetter && doc.isEmpty() && attributes.isEmpty() && modifiers.isEmpty()) {
      emitLocalTypes(codeWriter)
      codeWriter.emitCode(body)
      return
    }

    codeWriter.emitDoc(doc)
    codeWriter.emitAttributes(attributes)
    codeWriter.emitModifiers(modifiers, implicitModifiers)

    if (!isConstructor && !isDeinitializer && !type.isAccessor) {
      codeWriter.emit("func ")
    }

    emitSignature(codeWriter, enclosingName)
    codeWriter.emitWhereBlock(typeVariables)

    if (body !== CodeBlock.ABSTRACT) {
      codeWriter.emit(" {\n")
      codeWriter.indent()
      emitLocalTypes(codeWriter)
      codeWriter.emitCode(body)
      codeWriter.unindent()
      codeWriter.emit("}\n")
    }
  }

  private fun emitLocalTypes(codeWriter: CodeWriter) {
    if (localTypeSpecs.isEmpty()) {
      return
    }

    localTypeSpecs.forEach { typeSpec ->
      codeWriter.emit("\n")
      typeSpec.emit(codeWriter)
    }

    codeWriter.emit("\n")
  }

  private fun emitSignature(codeWriter: CodeWriter, enclosingName: String?) {
    if (isConstructor) {
      codeWriter.emitCode(CONSTRUCTOR, enclosingName)
      if (failable) {
        codeWriter.emit("?")
      }
    } else if (isDeinitializer) {
      codeWriter.emitCode(DEINITIALIZER, enclosingName)
      return
    } else if (type == Type.Getter) {
      codeWriter.emitCode(GETTER)
      return
    } else if (type == Type.Setter) {
      codeWriter.emitCode(SETTER)
      if (parameters.isEmpty()) {
        return
      }
    } else {
      val name =
        if (type == Type.Operator)
          name.removePrefix(OPERATOR)
        else
          escapeIfNecessary(name)
      codeWriter.emitCode("%L", name)
    }

    if (typeVariables.isNotEmpty()) {
      codeWriter.emitTypeVariables(typeVariables)
    }

    parameters.emit(codeWriter) { param ->
      param.emit(codeWriter, includeType = type != Type.Setter)
    }

    val modifiers = mutableListOf<String>()

    if (async) {
      modifiers.add("async")
    }
    if (throws) {
      modifiers.add("throws")
    }

    if (modifiers.isNotEmpty()) {
      codeWriter.emit(modifiers.joinToString(separator = " ", prefix = " "))
    }

    if (returnType != null && returnType != VOID) {
      codeWriter.emitCode(" -> %T", returnType)
    }
  }

  val isConstructor get() = type == Type.Initializer

  val isDeinitializer get() = type == Type.Deinitializer

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (javaClass != other.javaClass) return false
    return toString() == other.toString()
  }

  override fun hashCode() = toString().hashCode()

  override fun toString() = buildString {
    emit(CodeWriter(this), "Constructor", TypeSpec.Kind.Class().implicitFunctionModifiers)
  }

  fun toBuilder(): Builder {
    val builder = Builder(name)
    builder.doc.add(doc)
    builder.attributes += attributes
    builder.modifiers += modifiers
    builder.typeVariables += typeVariables
    builder.returnType = returnType
    builder.parameters += parameters
    builder.body.add(body)
    return builder
  }

  enum class Type(val isAccessor: Boolean) {
    Function(false),
    Operator(false),
    Getter(true),
    Setter(true),
    Initializer(false),
    Deinitializer(false),
  }

  class Builder internal constructor(
    internal val name: String,
    internal val type: Type = Type.Function,
  ) : AttributedSpec.Builder<Builder>() {

    internal val doc = CodeBlock.builder()
    internal val modifiers = mutableListOf<Modifier>()
    internal val typeVariables = mutableListOf<TypeVariableName>()
    internal var returnType: TypeName? = null
    internal val parameters = mutableListOf<ParameterSpec>()
    internal var throws = false
    internal var async = false
    internal var failable = false
    internal val localTypeSpecs = mutableListOf<AnyTypeSpec>()
    internal val body: CodeBlock.Builder = CodeBlock.builder()
    internal var abstract = false

    fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    fun addModifiers(vararg modifiers: Modifier) = apply {
      this.modifiers += modifiers
    }

    fun addModifiers(modifiers: Iterable<Modifier>) = apply {
      this.modifiers += modifiers
    }

    fun addTypeVariables(typeVariables: Iterable<TypeVariableName>) = apply {
      check(!type.isAccessor) { "$name cannot have type variables" }
      this.typeVariables += typeVariables
    }

    fun addTypeVariable(typeVariable: TypeVariableName) = apply {
      check(!type.isAccessor) { "$name cannot have type variables" }
      typeVariables += typeVariable
    }

    fun returns(returnType: TypeName) = apply {
      check(type != Type.Initializer && !type.isAccessor) { "$name cannot have a return type" }
      this.returnType = returnType
    }

    fun addParameters(parameterSpecs: Iterable<ParameterSpec>) = apply {
      for (parameterSpec in parameterSpecs) {
        addParameter(parameterSpec)
      }
    }

    fun addParameter(parameterSpec: ParameterSpec) = apply {
      check(type != Type.Getter) { "$name cannot have parameters" }
      check(type != Type.Setter || parameters.size == 0) { "$name can have only one parameter" }
      parameters += parameterSpec
    }

    fun addParameter(name: String, type: TypeName, vararg modifiers: Modifier) =
      addParameter(ParameterSpec.builder(name, type, *modifiers).build())

    fun addParameter(label: String, name: String, type: TypeName, vararg modifiers: Modifier) =
      addParameter(ParameterSpec.builder(label, name, type, *modifiers).build())

    fun addCode(format: String, vararg args: Any) = apply {
      body.add(format, *args)
    }

    fun abstract(value: Boolean) = apply {
      check(body.isEmpty()) { "function with code cannot be abstract" }
      abstract = value
    }

    fun failable(value: Boolean) = apply {
      check(type == Type.Initializer) { "only constructors can be failable" }
      failable = value
    }

    fun throws(value: Boolean) = apply {
      throws = value
    }

    fun async(value: Boolean) = apply {
      async = value
    }

    fun addLocalTypes(typeSpecs: Iterable<AnyTypeSpec>) = apply {
      check(!abstract) { "abstract functions cannot have local types" }
      this.localTypeSpecs += typeSpecs
    }

    fun addLocalType(typeSpec: AnyTypeSpec) = apply {
      localTypeSpecs += typeSpec
    }

    fun addNamedCode(format: String, args: Map<String, *>) = apply {
      check(!abstract) { "abstract functions cannot have code" }
      body.addNamed(format, args)
    }

    fun addCode(codeBlock: CodeBlock) = apply {
      check(!abstract) { "abstract functions cannot have code" }
      body.add(codeBlock)
    }

    fun addComment(format: String, vararg args: Any) = apply {
      body.add("// " + format + "\n", *args)
    }

    /**
     * @param controlFlowName the control flow construct (e.g. "if", "switch", etc.).
     * @param controlFlowCode code for control flow, such as "foo == 5"
     *     Shouldn't contain braces or newline characters.
     */
    fun beginControlFlow(controlFlowName: String, controlFlowCode: String, vararg args: Any) = apply {
      body.beginControlFlow(controlFlowName, controlFlowCode, *args)
    }

    /**
     * @param controlFlowName the control flow construct (e.g. "else if").
     * @param controlFlowCode the control flow construct and its code, such as "else if (foo == 10)".
     *     Shouldn't contain braces or newline characters.
     */
    fun nextControlFlow(controlFlowName: String, controlFlowCode: String, vararg args: Any?) = apply {
      body.nextControlFlow(controlFlowName, controlFlowCode, *args)
    }

    fun endControlFlow(controlFlowName: String) = apply {
      body.endControlFlow(controlFlowName)
    }

    fun addStatement(format: String, vararg args: Any) = apply {
      body.addStatement(format, *args)
    }

    fun build() = FunctionSpec(this)
  }

  companion object {

    private const val GETTER = "get"
    private const val SETTER = "set"
    private const val CONSTRUCTOR = "init"
    private const val DEINITIALIZER = "deinit"
    private const val OPERATOR = "op:"

    @JvmStatic
    fun builder(name: String) = Builder(name)

    @JvmStatic
    fun abstractBuilder(name: String) = Builder(name).abstract(true)

    @JvmStatic
    fun constructorBuilder() = Builder(special(CONSTRUCTOR), Type.Initializer)

    @JvmStatic
    fun deinitializerBuilder() = Builder(special(DEINITIALIZER), Type.Deinitializer)

    @JvmStatic
    fun getterBuilder() = Builder(special(GETTER), Type.Getter)

    @JvmStatic
    fun setterBuilder() = Builder(special(SETTER), Type.Setter)

    @JvmStatic
    fun operatorBuilder(name: String) = Builder(OPERATOR + name, Type.Operator)

    @JvmStatic
    private fun special(name: String) = "<$name>"
  }
}
