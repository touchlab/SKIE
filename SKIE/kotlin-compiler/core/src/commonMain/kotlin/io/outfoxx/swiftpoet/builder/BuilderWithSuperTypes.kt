package io.outfoxx.swiftpoet.builder

import io.outfoxx.swiftpoet.TypeName

interface BuilderWithSuperTypes<SELF : BuilderWithDocs<SELF>> {
    fun addSuperTypes(superTypes: Iterable<TypeName>): SELF

    fun addSuperType(superType: TypeName): SELF
}
