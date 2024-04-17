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
import io.outfoxx.swiftpoet.Modifier.PRIVATE
import io.outfoxx.swiftpoet.Modifier.PUBLIC
import io.outfoxx.swiftpoet.builder.BuilderWithModifiers
import io.outfoxx.swiftpoet.builder.BuilderWithTypeParameters

/** A generated typealias declaration */
class TypeAliasSpec private constructor(
  builder: Builder,
) : AnyTypeSpec(builder.name, builder.attributes, builder.tags) {

  val type = builder.type
  val modifiers = builder.modifiers.toImmutableSet()
  val typeVariables = builder.typeVariables.toImmutableList()
  val doc = builder.doc.build()

  override fun emit(codeWriter: CodeWriter) {
    codeWriter.emitDoc(doc)
    codeWriter.emitAttributes(attributes)
    codeWriter.emitModifiers(modifiers)
    codeWriter.emitCode("typealias %L", escapeIfNecessary(name))
    codeWriter.emitTypeVariables(typeVariables)
    codeWriter.emitCode(" = %T", type)
    codeWriter.emit("\n")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (javaClass != other.javaClass) return false
    return toString() == other.toString()
  }

  override fun hashCode() = toString().hashCode()

  override fun toString() = buildString { emit(CodeWriter(this)) }

  fun toBuilder(): Builder {
    val builder = Builder(name, type)
    builder.modifiers += modifiers
    builder.typeVariables += typeVariables
    builder.doc.add(doc)
    return builder
  }

  class Builder internal constructor(
    internal val name: String,
    internal val type: TypeName,
  ) : AttributedSpec.Builder<Builder>(), BuilderWithModifiers, BuilderWithTypeParameters {

    internal val doc = CodeBlock.builder()
    internal val modifiers = mutableSetOf<Modifier>()
    internal val typeVariables = mutableSetOf<TypeVariableName>()

    fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    override fun addModifiers(vararg modifiers: Modifier) = apply {
      modifiers.forEach(this::addModifier)
    }

    private fun addModifier(modifier: Modifier) {
      require(modifier in setOf(PUBLIC, INTERNAL, PRIVATE)) {
        "unexpected typealias modifier $modifier"
      }
      this.modifiers.add(modifier)
    }

    fun addTypeVariables(typeVariables: Iterable<TypeVariableName>) = apply {
      this.typeVariables += typeVariables
    }

    override fun addTypeVariable(typeVariable: TypeVariableName) = apply {
      typeVariables += typeVariable
    }

    fun build() = TypeAliasSpec(this)
  }

  companion object {

    @JvmStatic
    fun builder(name: String, type: TypeName) = Builder(name, type)
  }
}
