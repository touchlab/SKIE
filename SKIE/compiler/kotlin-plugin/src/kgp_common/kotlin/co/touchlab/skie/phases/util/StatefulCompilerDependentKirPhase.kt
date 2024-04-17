package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.CompilerDependentKirPhase

abstract class StatefulCompilerDependentKirPhase : CompilerDependentKirPhase,
    StatefulScheduledPhase<CompilerDependentKirPhase.Context>()
