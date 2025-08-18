package co.touchlab.skie.buildsetup.plugins.util

import co.touchlab.skie.gradle.KotlinToolingVersion
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class MultiCompileRuntimeExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
) {

    val isPublishable: Property<Boolean> = objects.property<Boolean>().convention(false)
    val targets: ListProperty<MultiCompileTarget> = objects.listProperty()
    val sourceDir: DirectoryProperty = objects.directoryProperty().convention(layout.projectDirectory.dir("impl"))
    val sourceIncludes: ListProperty<String> = objects.listProperty<String>().convention(
        listOf(
            "src/**",
            "build.gradle.kts",
            "gradle.properties",
            "settings.gradle.kts",
        ),
    )
    val rootKotlinVersion: Property<KotlinToolingVersion?> = objects.property<KotlinToolingVersion?>()
    val dependencies: Property<(KotlinToolingVersion) -> String> = objects.property<(KotlinToolingVersion) -> String>().convention { "" }
    val applyDependencies: Property<DependencyHandlerScope.(KotlinToolingVersion, Configuration) -> Unit> =
        objects.property<DependencyHandlerScope.(KotlinToolingVersion, Configuration) -> Unit>().convention { _, _ -> }
    val klibPath: Property<(KotlinToolingVersion, MultiCompileTarget) -> String> = objects.property()
}
