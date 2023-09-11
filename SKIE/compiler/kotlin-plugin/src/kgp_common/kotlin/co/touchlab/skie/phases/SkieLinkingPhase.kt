package co.touchlab.skie.phases

interface SkieLinkingPhase {

    val isActive: Boolean
        get() = true

    fun execute()
}
