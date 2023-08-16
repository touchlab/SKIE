package co.touchlab.skie.api.phases

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqIdentifier
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.module.SkieModule
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec

class FixNestedBridgedTypesPhase(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure {
            descriptorProvider.allExposedTypesSwiftModels
                .filter { it.needsTypeAliasForBridging }
                .forEach {
                    it.moveOutAndRenameOriginalClass()
                }
        }

        skieModule.file("SkieTypeAliasesForBridging") {
            descriptorProvider.allExposedTypesSwiftModels
                .filter { it.needsTypeAliasForBridging }
                .forEach {
                    it.appendTypeAliasForBridging()
                }
        }
    }

    context(SwiftModelScope)
    private val DescriptorProvider.allExposedTypesSwiftModels: List<KotlinTypeSwiftModel>
        get() = this.exposedClasses.map { it.swiftModel } + this.exposedFiles.map { it.swiftModel }

    context(MutableSwiftModelScope)
    private val DescriptorProvider.allExposedTypesSwiftModels: List<MutableKotlinTypeSwiftModel>
        get() = this.exposedClasses.map { it.swiftModel } + this.exposedFiles.map { it.swiftModel }

    private val KotlinTypeSwiftModel.needsTypeAliasForBridging: Boolean
        get() = bridge?.declaration?.publicName?.asString() != bridge?.declaration?.publicName?.safeForBridging

    // Moves the class outside its parent class and renames it to avoid name collisions.
    // This is a workaround for `typealias` thinking that it's recursive (probably a bug in Swift compiler).
    private fun MutableKotlinTypeSwiftModel.moveOutAndRenameOriginalClass() {
        this.identifier = this.fqIdentifier.replace(".", "___")
        this.containingType = null
    }

    context(FileSpec.Builder)
    private fun KotlinTypeSwiftModel.appendTypeAliasForBridging() {
        val bridge = bridge ?: error("Type $this does not have a bridge.")

        // TODO: The second parameter should be `SirType` and not `Declaration`. This will work until we need generic typealias.
        addType(
            TypeAliasSpec.builder(bridge.declaration.publicName.safeForBridging, bridge.declaration.internalName.toSwiftPoetName())
                .addModifiers(Modifier.PUBLIC)
                .build()
        )
    }
}

val SwiftFqName.safeForBridging: String
    get() = asString().replace(".", "__")
