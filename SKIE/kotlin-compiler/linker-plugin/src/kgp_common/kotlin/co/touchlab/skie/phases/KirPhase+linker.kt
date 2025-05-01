@file:Suppress("ktlint:standard:filename")

package co.touchlab.skie.phases

import co.touchlab.skie.context.KirPhaseContext
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.kir.type.translation.KirDeclarationTypeTranslator
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

val KirPhase.Context.descriptorKirProvider: DescriptorKirProvider
    get() = typedContext.descriptorKirProvider

val KirPhase.Context.namer: ObjCExportNamer
    get() = typedContext.namer

val KirPhase.Context.kirTypeTranslator: KirTypeTranslator
    get() = typedContext.kirTypeTranslator

val KirPhase.Context.kirDeclarationTypeTranslator: KirDeclarationTypeTranslator
    get() = typedContext.kirDeclarationTypeTranslator

val KirPhase.Context.objCExportedInterfaceProvider: ObjCExportedInterfaceProvider
    get() = typedContext.objCExportedInterfaceProvider

private val KirPhase.Context.typedContext: KirPhaseContext
    get() = context as KirPhaseContext
