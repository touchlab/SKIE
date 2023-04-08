package co.touchlab.skie.plugin.generator.internal.analytics

import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.nio.file.Path
import kotlin.io.path.writeBytes

class AirAnalyticsProducer(
    private val descriptorProvider: DescriptorProvider,
    private val modules: Collection<IrModuleFragment>,
) : AnalyticsProducer {

    private val json = Json { classDiscriminator = "jsonType" }

    override fun produce(): AnalyticsProducer.Result =
        AnalyticsProducer.Result(
            name = "air",
            data = IrToAirTransformer(descriptorProvider).transformToAir(modules).serialize(),
        )

    private fun AirProject.serialize(): ByteArray =
        json.encodeToString(this).toByteArray()
}
