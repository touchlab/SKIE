package co.touchlab.swiftgen.plugin.internal.util

import co.touchlab.swiftpack.api.kotlin
import io.outfoxx.swiftpoet.DeclaredTypeName
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.kotlinFqName

internal val IrClass.kotlinName: String
    get() = this.kotlinFqName.asString()

internal val IrClass.swiftName: DeclaredTypeName
    get() = DeclaredTypeName.kotlin(kotlinName)

internal val IrClass.isSealed: Boolean
    get() = modality == Modality.SEALED