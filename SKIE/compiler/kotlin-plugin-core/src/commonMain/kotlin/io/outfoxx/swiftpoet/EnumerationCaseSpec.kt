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

class EnumerationCaseSpec private constructor(
  builder: Builder,
) : AttributedSpec(builder.attributes.toImmutableList(), builder.tags) {

  val name = builder.name
  val typeOrConstant = builder.typeOrConstant
  val doc = builder.doc.build()

  fun toBuilder(): Builder {
    val builder = Builder(name, typeOrConstant)
    builder.doc.add(doc)
    builder.attributes += attributes
    return builder
  }

  internal fun emit(codeWriter: CodeWriter) {

    codeWriter.emitDoc(doc)
    codeWriter.emitAttributes(attributes)
    codeWriter.emitCode("case %L", escapeIfKeyword(name))
    when (typeOrConstant) {
      null -> {}
      is CodeBlock -> codeWriter.emitCode(" = %L", typeOrConstant)
      is TupleTypeName -> typeOrConstant.emit(codeWriter)
      else -> throw IllegalStateException("Invalid enum type of constant")
    }
  }

  class Builder internal constructor(
    internal var name: String,
    internal var typeOrConstant: Any?,
  ) : AttributedSpec.Builder<Builder>() {

    internal val doc = CodeBlock.builder()

    fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    fun build(): EnumerationCaseSpec {
      return EnumerationCaseSpec(this)
    }
  }

  companion object {

    @JvmStatic
    fun builder(name: String) = Builder(name, null)

    @JvmStatic
    fun builder(name: String, type: TypeName) = Builder(name, TupleTypeName.of("" to type))

    @JvmStatic
    fun builder(name: String, type: TupleTypeName) = Builder(name, type)

    @JvmStatic
    fun builder(name: String, constant: CodeBlock) = Builder(name, constant)

    @JvmStatic
    fun builder(name: String, constant: String) = Builder(name, CodeBlock.of("%S", constant))

    @JvmStatic
    fun builder(name: String, constant: Int) = Builder(name, CodeBlock.of("%L", constant.toString()))
  }
}
