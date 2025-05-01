package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.SirPhase

abstract class StatefulSirPhase :
    StatefulScheduledPhase<SirPhase.Context>(),
    SirPhase
