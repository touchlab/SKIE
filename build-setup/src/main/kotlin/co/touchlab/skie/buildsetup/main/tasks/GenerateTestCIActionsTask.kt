package co.touchlab.skie.buildsetup.main.tasks

import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersion
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.writeText

abstract class GenerateTestCIActionsTask : DefaultTask() {

    @get:Input
    abstract val supportedVersions: ListProperty<SupportedKotlinVersion>

    @get:OutputDirectory
    abstract val outputDirectory: RegularFileProperty

    init {
        group = "other"
        description = "Generates the smoke-tests workflow files."
    }

    @TaskAction
    fun execute() {
        val outputPath = outputDirectory.get().asFile.toPath()

        outputPath.createDirectories()

        outputPath.listDirectoryEntries()
            .filter { it.name.startsWith("smoke-tests-") && it.name["smoke-tests-".length].isDigit() }
            .forEach { it.deleteIfExists() }

        supportedVersions.get().forEach {
            val workflow = getSmokeTestsWorkflow(it)

            outputPath.resolve("smoke-tests-${it.safeName}.yml").writeText(workflow)
        }
    }

    private fun getSmokeTestsWorkflow(version: SupportedKotlinVersion): String = $$"""
        name: Smoke Tests $${version.name}

        on:
          workflow_dispatch:
            inputs:
              linkage:
                type: choice
                options:
                  - static
                  - dynamic
                required: true
                default: static
                description:
                  'The linkage mode to use for the tests. "static" will produce static frameworks, "dynamic" will produce dynamic frameworks.'
              configuration:
                type: choice
                options:
                  - debug
                  - release
                required: true
                default: debug
                description:
                  'The configuration to use for the tests. "release" will produce release builds, "debug" will produce debug builds (type mapping tests currently always use release).'
              target:
                type: choice
                options:
                  - ios_arm64
                  - ios_x64
                  - ios_simulator_arm64
                  - macos_arm64
                  - macos_x64
                required: true
                default: macos_arm64
                description:
                  'The target to use for the type mapping tests.'

        permissions:
          contents: read
          checks: write

        jobs:
          acceptance-tests:
            name: Acceptance Tests
            runs-on: macos-14
            steps:
              - name: Checkout Repo
                uses: actions/checkout@v3
                with:
                  submodules: true
                  token: ${{ secrets.ACCEPTANCE_TESTS_TOKEN }}
              - name: Prepare Worker
                uses: ./.github/actions/prepare-worker
              - name: Run Acceptance Tests
                uses: gradle/gradle-build-action@v2.4.2
                with:
                  arguments: ':acceptance-tests:functional:test -PversionSupport.kotlin.enabledVersion=$${version.name}'
                  build-root-directory: SKIE
                env:
                  KOTLIN_LINK_MODE: ${{ inputs.linkage }}
                  KOTLIN_BUILD_CONFIGURATION: ${{ inputs.configuration }}

          type-mapping-tests:
            name: Type Mapping Tests
            runs-on: macos-14
            steps:
              - name: Checkout Repo
                uses: actions/checkout@v3
                with:
                  submodules: true
                  token: ${{ secrets.ACCEPTANCE_TESTS_TOKEN }}
              - name: Prepare Worker
                uses: ./.github/actions/prepare-worker
              - name: Run Type Mapping Tests
                uses: gradle/gradle-build-action@v2.4.2
                id: run-tests
                with:
                  arguments: ':acceptance-tests:type-mapping:test -PversionSupport.kotlin.enabledVersions=$${version.name}'
                  build-root-directory: SKIE
                env:
                  KOTLIN_LINK_MODE: ${{ inputs.linkage }}
                  KOTLIN_TARGET: ${{ inputs.target }}
                  KOTLIN_BUILD_CONFIGURATION: ${{ inputs.configuration }}

          external-libraries-tests:
            name: External Libraries Tests
            runs-on: macos-14
            steps:
              - name: Checkout Repo
                uses: actions/checkout@v3
                with:
                  submodules: true
                  token: ${{ secrets.ACCEPTANCE_TESTS_TOKEN }}
              - name: Prepare Worker
                uses: ./.github/actions/prepare-worker
              - name: Run External Libraries Tests
                uses: gradle/gradle-build-action@v2.4.2
                with:
                  arguments: ':acceptance-tests:libraries:test -PversionSupport.kotlin.enabledVersions=$${version.name}'
                  build-root-directory: SKIE
                env:
                  KOTLIN_LINK_MODE: ${{ inputs.linkage }}
                  KOTLIN_BUILD_CONFIGURATION: ${{ inputs.configuration }}
    """.trimIndent()
}
