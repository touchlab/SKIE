package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.plugin.generator.internal.arguments.collision.CollisionDetector
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider

internal interface DefaultArgumentGeneratorDelegate : ConfigurationContainer {

    fun generate(descriptorProvider: DescriptorProvider, collisionDetector: CollisionDetector)
}
