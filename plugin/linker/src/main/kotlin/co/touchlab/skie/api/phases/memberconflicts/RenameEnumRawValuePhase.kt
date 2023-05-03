package co.touchlab.skie.api.phases.memberconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.module.SkieModule
import org.jetbrains.kotlin.descriptors.isEnumClass

// TODO This is a workaround - once we have SIR it will be easier to create proper IR representation for these build in declarations
class RenameEnumRawValuePhase(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.Last) {
            descriptorProvider.exposedClasses
                // TODO enums for which we do not generate wrapper do not need this workaround
                .filter { it.kind.isEnumClass }
                .map { it.swiftModel }
                .forEach {
                    it.renameCustomRawValueIfNeeded()
                }
        }
    }
}

private fun MutableKotlinClassSwiftModel.renameCustomRawValueIfNeeded() {
    allAccessibleDirectlyCallableMembers
        .filter { it.name == "rawValue" }
        .filterNot { it is KotlinRegularPropertySwiftModel && it.type.asString() == "Swift.String" }
        .forEach {
            it.identifier += "_"
        }
}
