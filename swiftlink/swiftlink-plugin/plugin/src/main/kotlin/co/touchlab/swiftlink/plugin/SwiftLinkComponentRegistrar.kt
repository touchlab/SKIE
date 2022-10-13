package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.intercept.PhaseInterceptor
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.resolve.lazy.LazyClassContext
import org.jetbrains.kotlin.resolve.lazy.declarations.PackageMemberDeclarationProvider

@AutoService(ComponentRegistrar::class)
class SwiftLinkComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        configuration.get(ConfigurationKeys.linkPhaseSwiftPackOutputDir)?.let {
            SwiftPackModuleBuilder.Config.outputDir = it
        }

        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}
