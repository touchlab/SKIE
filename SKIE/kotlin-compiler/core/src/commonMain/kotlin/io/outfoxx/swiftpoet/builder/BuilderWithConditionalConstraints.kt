package io.outfoxx.swiftpoet.builder

import io.outfoxx.swiftpoet.TypeVariableName

interface BuilderWithConditionalConstraints<SELF: BuilderWithConditionalConstraints<SELF>> {
  fun addConditionalConstraints(conditionalConstraints: Iterable<TypeVariableName>): SELF

  fun addConditionalConstraint(conditionalConstraint: TypeVariableName): SELF
}
