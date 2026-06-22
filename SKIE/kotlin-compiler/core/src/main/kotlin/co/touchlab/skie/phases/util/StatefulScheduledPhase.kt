package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.ScheduledPhase

abstract class StatefulScheduledPhase<C : ScheduledPhase.Context> : ScheduledPhase<C> {

    context(c: C)
    override suspend fun execute() {
        // Cannot be in the init because the object instance is not available yet.
        check(this::class.objectInstance != null) {
            "StatefulScheduledPhase ${this::class.qualifiedName} must be an object."
        }

        c.executeStatefulScheduledPhase(this, c)
    }
}

fun <T : StatefulScheduledPhase<C>, C : ScheduledPhase.Context> ScheduledPhase.Context.doInPhase(phase: T, action: C.() -> Unit) {
    storeStatefulScheduledPhaseBody(phase, action)
}
