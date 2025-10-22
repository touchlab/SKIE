package co.touchlab.skie.phases

import co.touchlab.skie.context.ClassExportPhaseContext
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

val ClassExportPhase.Context.declarationBuilder: DeclarationBuilder
    get() = typedContext.declarationBuilder

private val ClassExportPhase.Context.typedContext: ClassExportPhaseContext
    get() = context as ClassExportPhaseContext
