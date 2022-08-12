package co.touchlab.swiftlink.plugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import kotlin.reflect.jvm.jvmName

@AutoService(ComponentRegistrar::class)
class SwiftKtComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        SwiftLinkRunnerPhase.register(configuration)
    }
}
