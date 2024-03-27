package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface ClassExportPhase : SkiePhase<ClassExportPhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilder
    }
}
