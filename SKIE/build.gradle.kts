import co.touchlab.skie.buildsetup.plugins.DevRoot
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin

plugins {
    id("skie.root")
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.nexusPublish)
}

nexusPublishing {
    repositories {
        sonatype()
    }
}
