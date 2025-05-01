package co.touchlab.skie.plugin.util

import groovy.json.JsonOutput

fun Any.toPrettyJson(): String = JsonOutput.prettyPrint(JsonOutput.toJson(this))
