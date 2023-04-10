package co.touchlab.skie.plugin.generator.internal.analytics.air

import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class AirAnalyticsProducer(
    private val descriptorProvider: DescriptorProvider,
    private val modules: Collection<IrModuleFragment>,
) : AnalyticsProducer {

    override val name: String = "air"

    private val json = Json { classDiscriminator = "jsonType" }

    override fun produce(): ByteArray =
        IrToAirTransformer(descriptorProvider).transformToAir(modules).serialize()

    private fun AirProject.serialize(): ByteArray =
        json.encodeToString(this).toByteArray()
}
