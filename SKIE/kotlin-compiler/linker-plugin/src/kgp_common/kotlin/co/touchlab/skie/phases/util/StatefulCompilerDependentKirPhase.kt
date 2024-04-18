package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.KirPhase

abstract class StatefulCompilerDependentKirPhase : KirPhase,
    StatefulScheduledPhase<KirPhase.Context>()
