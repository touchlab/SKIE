package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.gradle_plugin_impl.BuildConfig
import co.touchlab.skie.util.directory.SkieApplicationSupportDirectory
import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import groovy.json.JsonSlurper
import java.io.File
import java.util.UUID
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

abstract class SkieUploadAnalyticsTask : DefaultTask() {

    @get:InputDirectory
    abstract val analyticsDirectory: Property<File>

    @get:Internal
    abstract val applicationSupportDirectory: Property<SkieApplicationSupportDirectory>

    init {
        this.doNotTrackState("The task has a side effect.")
    }

    @TaskAction
    fun runTask() {
        try {
            val analyticsId = getAnalyticsId()
            val eventData = getEventData()

            val json = JSONObject(eventData)

            val event = MessageBuilder(BuildConfig.MIXPANEL_PROJECT_TOKEN).event(analyticsId, "compile", json)

            MixpanelAPI().sendMessage(event)
        } catch (e: Throwable) {
            logger.warn("W: SKIE analytics upload failed: $e")
        }
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
                val parsedJson = file.parseJsonSafe()

                acc + mapOf(file.nameWithoutExtension to parsedJson)
            }
    }

    private fun File.parseJsonSafe(): Any = try {
        val fileContent = this.readText()

        JsonSlurper().parseText(fileContent)
    } catch (e: Throwable) {
        mapOf("error" to e.toString())
    }
}
