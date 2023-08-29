package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.cli.common.messages.GroupingMessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

class GroupingMessageCollectorReflector(
    override val instance: GroupingMessageCollector,
) : Reflector(GroupingMessageCollector::class.java) {

    var delegate by declaredField<MessageCollector>()
}
