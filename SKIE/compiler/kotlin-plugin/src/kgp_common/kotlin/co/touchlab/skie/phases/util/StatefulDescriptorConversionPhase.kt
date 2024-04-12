package co.touchlab.skie.phases.util

import co.touchlab.skie.phases.DescriptorConversionPhase

abstract class StatefulDescriptorConversionPhase : DescriptorConversionPhase, StatefulScheduledPhase<DescriptorConversionPhase.Context>()
