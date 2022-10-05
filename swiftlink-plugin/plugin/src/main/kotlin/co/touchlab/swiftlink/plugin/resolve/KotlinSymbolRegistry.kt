package co.touchlab.swiftlink.plugin.resolve

import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinMemberParentReference
import co.touchlab.swiftpack.spec.reference.KotlinPackageReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeParameterReference

class KotlinSymbolRegistry(
    references: Collection<KotlinDeclarationReference<*>>,
) {
    private val references = references.associateBy { it.id }

    operator fun get(packageId: KotlinPackageReference.Id): KotlinPackageReference = getReference(packageId)

    operator fun <ID: KotlinMemberParentReference.Id> get(memberParentId: ID): KotlinMemberParentReference<ID> = getReference(memberParentId)

    operator fun get(classId: KotlinClassReference.Id): KotlinClassReference = getReference(classId)

    operator fun get(propertyId: KotlinPropertyReference.Id): KotlinPropertyReference = getReference(propertyId)

    operator fun get(functionId: KotlinFunctionReference.Id): KotlinFunctionReference = getReference(functionId)

    operator fun get(typeParameterId: KotlinTypeParameterReference.Id): KotlinTypeParameterReference = getReference(typeParameterId)

    operator fun get(enumEntryId: KotlinEnumEntryReference.Id): KotlinEnumEntryReference = getReference(enumEntryId)

    private fun <R: KotlinDeclarationReference<ID>, ID: KotlinDeclarationReference.Id> getReference(
        symbolId: ID,
    ): R = requireNotNull(references[symbolId]) {
        "Symbol not found: $symbolId"
    } as R
}
