package io.outfoxx.swiftpoet.builder

import io.outfoxx.swiftpoet.TypeVariableName

interface BuilderWithAssociatedTypes<SELF: BuilderWithAssociatedTypes<SELF>>: BuilderWithTypeParameters {
  fun addAssociatedType(typeVariable: TypeVariableName): SELF
}
