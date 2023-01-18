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
            descriptorProvider.allExportedTypesMutableSwiftModels
                .filter { it.needsTypeAliasForBridging }
                .forEach {
                    it.identifier = listOfNotNull(it.containingType?.identifier, it.identifier).joinToString("__")
                    it.containingType = null
                }
        }

        skieModule.file("SkieTypeAliasesForBridging") {
            descriptorProvider.allExportedTypesSwiftModels
                .filter { it.needsTypeAliasForBridging }
                .forEach {
                    it.appendTypeAliasForBridging()
                }
        }
    }

    context(SwiftModelScope)
    private val DescriptorProvider.allExportedTypesSwiftModels: List<KotlinTypeSwiftModel>
        get() = exportedClassDescriptors.map { it.swiftModel } + exportedFiles.map { it.swiftModel }

    context(MutableSwiftModelScope)
    private val DescriptorProvider.allExportedTypesMutableSwiftModels: List<MutableKotlinTypeSwiftModel>
        get() = exportedClassDescriptors.map { it.swiftModel } + exportedFiles.map { it.swiftModel }
    
    val KotlinTypeSwiftModel.needsTypeAliasForBridging: Boolean
        get() = bridge?.fqName != bridge?.fqNameSafeForBridging

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
