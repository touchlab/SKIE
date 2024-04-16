package co.touchlab.skie.phases

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.kir.type.translation.KirDeclarationTypeTranslator
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

interface DescriptorConversionPhase : CompilerDependentForegroundPhase<DescriptorConversionPhase.Context> {

    interface Context : CompilerDependentForegroundPhase.Context {

        override val context: Context

        val kirProvider: KirProvider

        val descriptorKirProvider: DescriptorKirProvider

        val kirBuiltins: KirBuiltins

        val namer: ObjCExportNamer

        val kirTypeTranslator: KirTypeTranslator

        val kirDeclarationTypeTranslator: KirDeclarationTypeTranslator

        val objCExportedInterfaceProvider: ObjCExportedInterfaceProvider
    }
}
