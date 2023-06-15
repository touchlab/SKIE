package co.touchlab.skie.util.wip

@Suppress("FunctionName")
fun WIP(message: String = "Work in progress that must be completed before deployment"): Nothing =
    throw NotImplementedError(message)
