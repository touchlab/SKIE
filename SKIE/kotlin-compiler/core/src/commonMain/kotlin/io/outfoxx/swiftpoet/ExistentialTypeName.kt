package io.outfoxx.swiftpoet

class ExistentialTypeName(
    val type: TypeName,
): TypeName() {
  override fun emit(out: CodeWriter): CodeWriter {
    out.emit("any ")
    type.emit(out)
    return out
  }
}
