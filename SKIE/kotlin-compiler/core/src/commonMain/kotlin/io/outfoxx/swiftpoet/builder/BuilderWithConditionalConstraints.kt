package io.outfoxx.swiftpoet.builder

import io.outfoxx.swiftpoet.TypeVariableName

interface BuilderWithConditionalConstraints<SELF: BuilderWithConditionalConstraints<SELF>> {
  fun addConditionalConstraints(typeVariables: Iterable<TypeVariableName>): SELF

  fun addConditionalConstraint(typeVariable: TypeVariableName): SELF
}
