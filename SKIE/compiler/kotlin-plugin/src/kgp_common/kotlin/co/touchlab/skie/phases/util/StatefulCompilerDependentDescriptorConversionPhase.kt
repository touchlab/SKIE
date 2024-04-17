package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.CompilerDependentDescriptorConversionPhase

abstract class StatefulCompilerDependentDescriptorConversionPhase : CompilerDependentDescriptorConversionPhase,
    StatefulScheduledPhase<CompilerDependentDescriptorConversionPhase.Context>()
