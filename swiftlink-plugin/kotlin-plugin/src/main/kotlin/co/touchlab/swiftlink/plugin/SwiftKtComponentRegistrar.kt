package co.touchlab.swiftlink.plugin

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
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmName

@AutoService(ComponentRegistrar::class)
class SwiftKtComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        configuration.put(ConfigurationKeys.isEnabled, true)
        val phase = this::class.java.classLoader
            .loadClass("org.jetbrains.kotlin.backend.konan.ToplevelPhasesKt")
            .getDeclaredMethod("getObjectFilesPhase")
            .invoke(null)

        val field = phase.javaClass
            .getDeclaredField("lower")

        check(field.trySetAccessible()) { "Failed to make field `lower` accessible" }

        val currentPhase = field.get(phase) as CompilerPhase<CommonBackendContext, Unit, Unit>
        val originalPhase = if (currentPhase.javaClass.name == SwiftKtObjectFilesPhase::class.jvmName) {
            currentPhase.javaClass.getDeclaredField("originalPhase").let { field ->
                check(field.trySetAccessible()) { "Failed to make field `originalPhase` accessible" }
                field.get(currentPhase) as CompilerPhase<CommonBackendContext, Unit, Unit>
            }
        } else {
            currentPhase
        }
        val swiftKtObjectFilesPhase = SwiftKtObjectFilesPhase(originalPhase)
        field.set(phase, swiftKtObjectFilesPhase)
    }
}
