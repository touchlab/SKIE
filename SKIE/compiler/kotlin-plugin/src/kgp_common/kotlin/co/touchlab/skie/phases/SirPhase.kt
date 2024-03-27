package co.touchlab.skie.phases

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import co.touchlab.skie.oir.OirProvider
import co.touchlab.skie.oir.builtin.OirBuiltins
import co.touchlab.skie.oir.type.translation.OirTypeTranslator
import co.touchlab.skie.phases.oir.util.ExternalApiNotesProvider
import co.touchlab.skie.sir.ClassNamespaceProvider
import co.touchlab.skie.sir.SirFileProvider
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.type.translation.SirTypeTranslator
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

interface SirPhase : SkiePhase<SirPhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context

        val kirProvider: KirProvider

        val oirProvider: OirProvider

        val sirProvider: SirProvider

        val sirFileProvider: SirFileProvider

        val kirBuiltins: KirBuiltins

        val oirBuiltins: OirBuiltins

        val sirBuiltins: SirBuiltins

        val classNamespaceProvider: ClassNamespaceProvider

        val externalApiNotesProvider: ExternalApiNotesProvider

        val namer: ObjCExportNamer

        val kirTypeTranslator: KirTypeTranslator

        val oirTypeTranslator: OirTypeTranslator

        val sirTypeTranslator: SirTypeTranslator

        val objCExportedInterfaceProvider: ObjCExportedInterfaceProvider
    }
}
