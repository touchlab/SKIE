package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.bridging.DirectMembersPassthroughGenerator
import co.touchlab.skie.sir.element.SirClass
import io.outfoxx.swiftpoet.CodeBlock

object ExhaustiveEnumsMembersPassthroughGenerator {
    context(SirPhase.Context)
    fun generatePassthroughForMembers(enumKirClass: KirClass, bridgedEnum: SirClass) {
        DirectMembersPassthroughGenerator.generatePassthroughForMembers(
            targetBridge = bridgedEnum,
            bridgedKirClass = enumKirClass,
            delegateAccessor = CodeBlock.of("(self as _ObjectiveCType)")
        )
    }
}
