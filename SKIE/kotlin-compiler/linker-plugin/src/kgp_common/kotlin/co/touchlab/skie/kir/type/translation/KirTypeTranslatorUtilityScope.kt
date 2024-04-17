package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.NonNullReferenceKirType
import co.touchlab.skie.kir.type.NullableReferenceKirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.kir.type.SpecialOirKirType
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import org.jetbrains.kotlin.backend.konan.binaryRepresentationIsNullable
import org.jetbrains.kotlin.types.KotlinType

abstract class KirTypeTranslatorUtilityScope {

    protected fun NonNullReferenceKirType.withNullabilityOf(kotlinType: KotlinType): ReferenceKirType =
        this.withNullabilityOf(kotlinType.binaryRepresentationIsNullable())

    protected fun NonNullReferenceKirType.withNullabilityOf(nullable: Boolean): ReferenceKirType =
        if (nullable) {
            NullableReferenceKirType(this)
        } else {
            this
        }

    protected fun OirType.toKirType(): KirType =
        OirBasedKirType(this)

    protected fun SpecialReferenceOirType.toKirType(): SpecialOirKirType =
        SpecialOirKirType(this)
}
