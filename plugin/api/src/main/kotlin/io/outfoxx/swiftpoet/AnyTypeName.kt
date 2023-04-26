package io.outfoxx.swiftpoet

class AnyTypeName private constructor(): TypeName() {
  override fun emit(out: CodeWriter): CodeWriter {
    out.emit("Any")
    return out
  }

  companion object {
    val INSTANCE = AnyTypeName()
  }

}
