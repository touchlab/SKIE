@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper

internal interface InternalDescriptorProvider: DescriptorProvider {
    val mapper: ObjCExportMapper
}
