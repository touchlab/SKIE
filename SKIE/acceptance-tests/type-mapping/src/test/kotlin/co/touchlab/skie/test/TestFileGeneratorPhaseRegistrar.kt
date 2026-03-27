package co.touchlab.skie.test

import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.swift.ConvertSirIrFilesToSourceFilesPhase
import co.touchlab.skie.spi.SkiePluginRegistrar

class TestFileGeneratorPhaseRegistrar : SkiePluginRegistrar {

    override fun register(initPhaseContext: InitPhase.Context) {
        initPhaseContext.skiePhaseScheduler.sirPhases.modify {
            val indexOfCodeGenerator = indexOfFirst { phase -> phase is ConvertSirIrFilesToSourceFilesPhase }

            add(indexOfCodeGenerator, TestFileGenerationPhase)
        }
    }
}
