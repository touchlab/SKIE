package co.touchlab.swiftkt.plugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.Checker
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.NamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class SwiftKtComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val phase = this::class.java.classLoader
            .loadClass("org.jetbrains.kotlin.backend.konan.ToplevelPhasesKt")
            .getDeclaredMethod("getObjectFilesPhase")
            .invoke(null)

        val field = phase.javaClass
            .getDeclaredField("lower")

        check(field.trySetAccessible()) { "Failed to make field `lower` accessible" }

        val originalPhase = field.get(phase) as CompilerPhase<CommonBackendContext, Unit, Unit>
        val modules = configuration.getList(ConfigurationKeys.swiftPackModules)
        val swiftSources = configuration.getList(ConfigurationKeys.swiftSourceFiles)
        val expandedSwiftDir = configuration.getNotNull(ConfigurationKeys.expandedSwiftDir)
        val compilePhase = SwiftKtCompilePhase(modules, swiftSources, expandedSwiftDir)
        val swiftKtObjectFilesPhase = SwiftKtObjectFilesPhase(originalPhase, compilePhase) {
            field.set(phase, originalPhase)
        }
        field.set(phase, swiftKtObjectFilesPhase)
    }
}
