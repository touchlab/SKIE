package io.outfoxx.swiftpoet

class OpaqueTypeName(
    val type: TypeName,
): TypeName() {
  override fun emit(out: CodeWriter): CodeWriter {
    out.emit("some ")
    type.emit(out)
    return out
  }
}
