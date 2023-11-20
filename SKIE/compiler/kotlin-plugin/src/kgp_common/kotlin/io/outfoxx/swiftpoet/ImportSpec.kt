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

class ImportSpec internal constructor(
  builder: Builder,
) : AttributedSpec(builder.attributes.toImmutableList(), builder.tags), Comparable<ImportSpec> {

  val name = builder.name
  val doc = builder.doc.build()
  val guardTest = builder.guardTest.build()

  private val importString = buildString {
    append(name)
  }

  internal fun emit(out: CodeWriter): CodeWriter {

    out.emitDoc(doc)

    if (guardTest.isNotEmpty()) {
      out.emit("#if ")
      out.emitCode(guardTest)
      out.emit("\n")
    }

    out.emitAttributes(attributes, suffix = " ")
    out.emit("import $name")

    if (guardTest.isNotEmpty()) {
      out.emit("\n")
      out.emit("#endif")
    }

    out.emit("\n")

    return out
  }

  override fun toString() = importString

  override fun compareTo(other: ImportSpec) = importString.compareTo(other.importString)

  class Builder internal constructor(
    internal val name: String,
  ) : AttributedSpec.Builder<Builder>() {

    internal val doc = CodeBlock.builder()
    internal val guardTest = CodeBlock.builder()

    fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    fun addGuard(test: CodeBlock) = apply {
      guardTest.add(test)
    }

    fun addGuard(format: String, vararg args: Any) = apply {
      addGuard(CodeBlock.of(format, args))
    }

    fun build(): ImportSpec {
      return ImportSpec(this)
    }
  }

  companion object {

    @JvmStatic
    fun builder(name: String) = Builder(name)
  }
}
