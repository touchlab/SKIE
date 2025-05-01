package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.KirPhase

abstract class StatefulKirPhase :
    StatefulScheduledPhase<KirPhase.Context>(),
    KirPhase
