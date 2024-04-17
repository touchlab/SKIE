package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.kir.type.translation.KirDeclarationTypeTranslator
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

interface CompilerDependentKirPhase :
    KirPhase<CompilerDependentKirPhase.Context>,
    CompilerDependentForegroundPhase<CompilerDependentKirPhase.Context> {

    interface Context : KirPhase.Context, CompilerDependentForegroundPhase.Context {

        override val context: Context

        val descriptorKirProvider: DescriptorKirProvider

        val namer: ObjCExportNamer

        val kirTypeTranslator: KirTypeTranslator

        val kirDeclarationTypeTranslator: KirDeclarationTypeTranslator

        val objCExportedInterfaceProvider: ObjCExportedInterfaceProvider
    }
}
