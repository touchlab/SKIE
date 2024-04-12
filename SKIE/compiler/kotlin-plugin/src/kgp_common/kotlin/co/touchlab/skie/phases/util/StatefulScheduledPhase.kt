package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.ScheduledPhase

abstract class StatefulScheduledPhase<C : ScheduledPhase.Context> : ScheduledPhase<C> {

    context(C)
    override suspend fun execute() {
        // Cannot be in the init because the object instance is not available yet.
        check(this::class.objectInstance != null) {
            "StatefulScheduledPhase ${this::class.qualifiedName} must be an object."
        }

        executeStatefulScheduledPhase(this, this@C)
    }
}

fun <T : StatefulScheduledPhase<C>, C : ScheduledPhase.Context> ScheduledPhase.Context.doInPhase(phase: T, action: C.() -> Unit) {
    storeStatefulScheduledPhaseBody(phase, action)
}
