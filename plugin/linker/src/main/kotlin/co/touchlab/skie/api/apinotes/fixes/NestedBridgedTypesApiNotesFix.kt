package co.touchlab.skie.api.apinotes.fixes

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec

class NestedBridgedTypesApiNotesFix(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) {

    fun createTypeAliasesForBridgingFile() {
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
        get() = this.transitivelyExposedClasses.map { it.swiftModel } + this.exposedFiles.map { it.swiftModel }

    context(MutableSwiftModelScope)
    private val DescriptorProvider.allExposedTypesSwiftModels: List<MutableKotlinTypeSwiftModel>
        get() = this.transitivelyExposedClasses.map { it.swiftModel } + this.exposedFiles.map { it.swiftModel }

    private val KotlinTypeSwiftModel.needsTypeAliasForBridging: Boolean
        get() = bridge?.fqName != bridge?.fqNameSafeForBridging

    // Moves the class outside its parent class and renames it to avoid name collisions.
    // This is a workaround for `typealias` thinking that it's recursive (probably a bug in Swift compiler).
    private fun MutableKotlinTypeSwiftModel.moveOutAndRenameOriginalClass() {
        this.identifier = this.fqIdentifier.replace(".", "__")
        this.containingType = null
    }

    private val KotlinTypeSwiftModel.fqIdentifier: String
        get() {
            val parentIdentifier = containingType?.fqIdentifier

            return if (parentIdentifier != null) "$parentIdentifier.$identifier" else identifier
        }

    context(FileSpec.Builder)
    private fun KotlinTypeSwiftModel.appendTypeAliasForBridging() {
        val bridge = bridge ?: error("Type $this does not have a bridge.")

        addType(
            TypeAliasSpec.builder(bridge.fqNameSafeForBridging, DeclaredTypeName.qualifiedLocalTypeName(bridge.fqName))
                .addModifiers(Modifier.PUBLIC)
                .build()
        )
    }
}

val TypeSwiftModel.fqNameSafeForBridging: String
    get() = fqName.replace(".", "__")
