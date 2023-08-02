package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.util.directory.SkieApplicationSupportDirectory
import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject
import java.io.File
import java.util.UUID

internal abstract class SkieUploadAnalyticsTask : DefaultTask() {

    @get:InputDirectory
    abstract val analyticsDirectory: Property<File>

    @get:Internal
    abstract val applicationSupportDirectory: Property<SkieApplicationSupportDirectory>

    @TaskAction
    fun runTask() {
        val projectToken = BuildConfig.MIXPANEL_PROJECT_TOKEN ?: return

        val analyticsId = getAnalyticsId()
        val eventData = getEventData()

        val json = JSONObject(eventData)

        val event = MessageBuilder(projectToken).event(analyticsId, "compile", json)

        MixpanelAPI().sendMessage(event)
    }

    private fun getAnalyticsId(): String {
        val analyticsIdFile = applicationSupportDirectory.get().analyticsId

        if (!analyticsIdFile.exists()) {
            return createNewAnalyticsId()
        }

        val existingUUID = analyticsIdFile.readText()

        if (existingUUID.length != 36) {
            return createNewAnalyticsId()
        }

        return existingUUID
    }

    private fun createNewAnalyticsId(): String {
        val newUUID = UUID.randomUUID().toString()

        val analyticsIdFile = applicationSupportDirectory.get().analyticsId

        analyticsIdFile.writeText(newUUID)

        return newUUID
    }

    private fun getEventData(): Map<String, Any> {
        val directory = analyticsDirectory.get()

        return directory.walkTopDown()
            .filter { it.extension == "json" }
            .fold(emptyMap()) { acc, file ->
                val fileContent = file.readText()

                val parsedJson = JsonSlurper().parseText(fileContent)

                acc + mapOf(file.nameWithoutExtension to parsedJson)
            }
    }
}
