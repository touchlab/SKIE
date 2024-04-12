package co.touchlab.skie.plugin.util

import groovy.json.JsonOutput

internal fun Any.toPrettyJson(): String =
    JsonOutput.prettyPrint(JsonOutput.toJson(this))
