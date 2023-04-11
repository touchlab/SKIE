package co.touchlab.skie.plugin.generator.internal.analytics.air

import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import kotlin.reflect.KClass

class AirAnalyticsProducer(
    private val descriptorProvider: DescriptorProvider,
    private val modules: Collection<IrModuleFragment>,
) : AnalyticsProducer<AnalyticsFeature.Air> {

    override val featureType: KClass<AnalyticsFeature.Air> = AnalyticsFeature.Air::class

    override val name: String = "air"

    private val json = Json { classDiscriminator = "jsonType" }

    override fun produce(configuration: AnalyticsFeature.Air): ByteArray =
        getAir(configuration).serialize()

    private fun getAir(configuration: AnalyticsFeature.Air): AirProject =
        if (configuration.stripIdentifiers) {
            getFullAir().transform(AirAnalyticsAnonymizer, Unit)
        } else {
            getFullAir()
        }

    private fun getFullAir(): AirProject =
        IrToAirTransformer(descriptorProvider).transformToAir(modules)

    private fun AirProject.serialize(): ByteArray =
        json.encodeToString(this).toByteArray()
}
