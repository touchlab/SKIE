package io.outfoxx.swiftpoet.builder

import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec

interface BuilderWithMembers {

    fun addProperty(propertySpec: PropertySpec): BuilderWithMembers

    fun addFunction(functionSpec: FunctionSpec): BuilderWithMembers
}
