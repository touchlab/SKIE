package io.outfoxx.swiftpoet.builder

import io.outfoxx.swiftpoet.CodeBlock

interface BuilderWithDocs<SELF: BuilderWithDocs<SELF>> {
  fun addDoc(format: String, vararg args: Any): SELF

  fun addDoc(block: CodeBlock): SELF
}
