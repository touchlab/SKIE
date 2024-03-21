package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.ClassExportPhase
import org.jetbrains.kotlin.library.KLIB_PROPERTY_SHORT_NAME
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName

// Fix for some libraries having ":" in their short name which resulted in invalid Obj-C header
object FixLibrariesShortNamePhase : ClassExportPhase {

    context(ClassExportPhase.Context)
    override suspend fun execute() {
        descriptorProvider.resolvedLibraries.forEach { library ->
            if (library.shortName == null) {
                library.manifestProperties.setProperty(
                    KLIB_PROPERTY_SHORT_NAME,
                    library.uniqueName.substringAfterLast(':'),
                )
            }
        }
    }
}
