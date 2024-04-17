package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.KirCompilerPhase

abstract class StatefulCompilerDependentKirPhase : KirCompilerPhase,
    StatefulScheduledPhase<KirCompilerPhase.Context>()
