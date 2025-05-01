@file:Suppress("ktlint:standard:filename")

package co.touchlab.skie.phases

import co.touchlab.skie.context.KotlinIrPhaseContext
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl

val KotlinIrPhase.Context.declarationBuilder: DeclarationBuilderImpl
    get() = typedContext.declarationBuilder

val KotlinIrPhase.Context.moduleFragment: IrModuleFragment
    get() = typedContext.moduleFragment

val KotlinIrPhase.Context.pluginContext: IrPluginContext
    get() = typedContext.pluginContext

val KotlinIrPhase.Context.skieSymbolTable: SkieSymbolTable
    get() = typedContext.skieSymbolTable

val KotlinIrPhase.Context.allModules: Map<String, IrModuleFragment>
    get() = typedContext.allModules

val KotlinIrPhase.Context.irBuiltIns: IrBuiltIns
    get() = pluginContext.irBuiltIns

val KotlinIrPhase.Context.irFactory: IrFactory
    get() = IrFactoryImpl

private val KotlinIrPhase.Context.typedContext: KotlinIrPhaseContext
    get() = context as KotlinIrPhaseContext
