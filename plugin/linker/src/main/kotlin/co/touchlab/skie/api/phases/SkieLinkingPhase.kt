package co.touchlab.skie.api.phases

interface SkieLinkingPhase {
    val isActive: Boolean
        get() = true

    fun execute()
}
