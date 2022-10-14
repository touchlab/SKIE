package co.touchlab.swiftlink.plugin

import co.touchlab.swiftpack.spec.module.SwiftPackModule
import org.jetbrains.kotlin.config.CompilerConfiguration

class SwiftPackModuleLoader(configuration: CompilerConfiguration) {

    private val localModuleReferences by lazy {
        configuration.get(ConfigurationKeys.linkPhaseSwiftPackOutputDir)?.let {
            SwiftPackModule.moduleReferencesInDir("link-phase", it)
        } ?: emptyList()
    }

    private val allModuleReferences by lazy {
        configuration.getList(ConfigurationKeys.swiftPackModules) + localModuleReferences
    }


    val modules by lazy {
        allModuleReferences.flatMap { (namespace, moduleFile) ->
            if (moduleFile.isDirectory) {
                moduleFile.listFiles()?.map {
                    SwiftPackModule.read(it).namespaced(namespace)
                } ?: emptyList()
            } else {
                listOf(SwiftPackModule.read(moduleFile).namespaced(namespace))
            }
        }
    }

    val references by lazy {
        modules.flatMap { it.references }.toSet()
    }

    val transforms by lazy {
        modules.flatMap { it.transforms }.toSet()
    }
}
