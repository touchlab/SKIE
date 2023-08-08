package co.touchlab.skie.analytics.modules

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.util.hashed
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.moduleName
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrOverridableDeclaration
import org.jetbrains.kotlin.ir.declarations.IrOverridableMember
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

@OptIn(ObsoleteDescriptorBasedAPI::class)
object AnonymousDeclarationsAnalytics {

    @Serializable
    data class Module(
        val id: String,
        val isExported: Boolean,
        val type: Type,
        val statistics: Statistics,
    ) {

        enum class Type {
            BuiltIn, Library, Local,
        }

        @Serializable
        data class Statistics(
            val exportedDeclarations: Declarations,
            val exportableNonExportedDeclarations: Declarations,
            val nonExportableDeclarations: Declarations,
            val overriddenCallableMembers: Int,
            val numberOfIrElements: Int,
        ) {

            @Serializable
            data class Declarations(
                val classes: Int,
                val callableMembers: Int,
            )
        }
    }

    private sealed interface TypedModule {

        val id: String

        val type: Module.Type

        val irModuleFragments: List<IrModuleFragment>

        fun isExported(descriptorProvider: DescriptorProvider): Boolean =
            irModuleFragments.any {
                it.descriptor in descriptorProvider.exposedModules
            }

        data class BuiltIn(
            override val irModuleFragments: List<IrModuleFragment>,
        ) : TypedModule {

            override val id: String = "stdlib"

            override val type: Module.Type = Module.Type.BuiltIn
        }

        data class Library(
            val name: String,
            override val irModuleFragments: List<IrModuleFragment>,
        ) : TypedModule {

            override val id: String = name

            override val type: Module.Type = Module.Type.Library
        }

        data class Local(
            override val id: String,
            val irModuleFragment: IrModuleFragment,
        ) : TypedModule {

            override val irModuleFragments: List<IrModuleFragment> = listOf(irModuleFragment)

            override val type: Module.Type = Module.Type.Local
        }
    }

    class Producer(
        private val config: KonanConfig,
        private val modules: Map<String, IrModuleFragment>,
        private val descriptorProvider: DescriptorProvider,
    ) : AnalyticsProducer {

        override val name: String = "anonymous-declarations"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Anonymous_Declarations

        override fun produce(): String {
            val allModules = getBuiltInModules() + getExternalLibraries() + getLocalModules()

            return allModules
                .map { it.toModuleWithStatistics() }
                .toPrettyJson()
        }

        private fun getBuiltInModules(): List<TypedModule> =
            config.resolvedLibraries.getFullList().filter { it.isDefault }
                .let { builtInLibraries ->
                    TypedModule.BuiltIn(builtInLibraries.map { getModuleForKlib(it.libraryFile.absolutePath) })
                }
                .let { listOf(it) }

        private fun getExternalLibraries(): List<TypedModule> =
            config.externalLibraries
                .map {
                    TypedModule.Library(it.canonicalName, it.artifactPaths.map { artifactPath -> getModuleForKlib(artifactPath.path) })
                }

        private fun getLocalModules(): List<TypedModule> =
            config.localModules
                .map { TypedModule.Local(it.localModuleId, getModuleForKlib(it.libraryFile.absolutePath)) }

        private fun getModuleForKlib(klib: String): IrModuleFragment =
            modules.getValue(klib.removeSuffix(".klib"))

        private fun TypedModule.toModuleWithStatistics(): Module {
            val statisticsVisitor = StatisticsVisitor(descriptorProvider)

            this.irModuleFragments.forEach {
                it.acceptVoid(statisticsVisitor)
            }

            return Module(
                id = this.id,
                isExported = this.isExported(descriptorProvider),
                type = this.type,
                statistics = statisticsVisitor.getStatistics(),
            )
        }
    }

    class StatisticsVisitor(private val descriptorProvider: DescriptorProvider) : IrElementVisitorVoid {

        private var exportedClasses = 0
        private var exportedCallableMembers = 0

        private var exportableNonExportedClasses = 0
        private var exportableNonExportedCallableMembers = 0

        private var nonExportableClasses = 0
        private var nonExportableCallableMembers = 0

        private var overriddenCallableMembers = 0

        private var numberOfIrElements = 0

        // Does not count classes and functions inside callable members towards declarations, however counts them towards numberOfIrElements
        private var isNestedInsideCallableMember = false

        fun getStatistics(): Module.Statistics =
            Module.Statistics(
                exportedDeclarations = Module.Statistics.Declarations(
                    classes = exportedClasses,
                    callableMembers = exportedCallableMembers
                ),
                exportableNonExportedDeclarations = Module.Statistics.Declarations(
                    classes = exportableNonExportedClasses,
                    callableMembers = exportableNonExportedCallableMembers
                ),
                nonExportableDeclarations = Module.Statistics.Declarations(
                    classes = nonExportableClasses,
                    callableMembers = nonExportableCallableMembers
                ),
                overriddenCallableMembers = overriddenCallableMembers,
                numberOfIrElements = numberOfIrElements,
            )

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)

            numberOfIrElements++
        }

        override fun visitClass(declaration: IrClass) {
            super.visitClass(declaration)

            if (isNestedInsideCallableMember) {
                return
            }

            if (declaration.descriptor in descriptorProvider.exposedClasses) {
                exportedClasses++
            } else if (descriptorProvider.isExposable(declaration.descriptor)) {
                exportableNonExportedClasses++
            } else {
                nonExportableClasses++
            }
        }

        override fun visitFunction(declaration: IrFunction) {
            if (isNestedInsideCallableMember) {
                super.visitFunction(declaration)

                return
            }

            isNestedInsideCallableMember = true

            super.visitFunction(declaration)

            if ((declaration as? IrOverridableDeclaration<*>)?.overriddenSymbols?.isNotEmpty() != true) {
                if (descriptorProvider.isExposed(declaration.descriptor)) {
                    exportedCallableMembers++
                } else if (descriptorProvider.isExposable(declaration.descriptor)) {
                    exportableNonExportedCallableMembers++
                } else {
                    nonExportableCallableMembers++
                }
            } else {
                overriddenCallableMembers++
            }

            isNestedInsideCallableMember = false
        }

        override fun visitProperty(declaration: IrProperty) {
            if (isNestedInsideCallableMember) {
                super.visitProperty(declaration)

                return
            }

            isNestedInsideCallableMember = true

            super.visitProperty(declaration)

            if (declaration.overriddenSymbols.isEmpty()) {
                if (descriptorProvider.isExposed(declaration.descriptor) ||
                    declaration.getter?.descriptor?.let { descriptorProvider.isExposed(it) } == true ||
                    declaration.setter?.descriptor?.let { descriptorProvider.isExposed(it) } == true
                ) {
                    exportedCallableMembers++
                } else if (descriptorProvider.isExposable(declaration.descriptor)) {
                    exportableNonExportedCallableMembers++
                } else {
                    nonExportableCallableMembers++
                }
            } else {
                overriddenCallableMembers++
            }

            isNestedInsideCallableMember = false
        }
    }
}
