package co.touchlab.skie.buildsetup.main.tasks

import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersion
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

abstract class GenerateAllVersionsSmokeTestsCIActionsTask : DefaultTask() {

    @get:Input
    abstract val supportedVersions: ListProperty<SupportedKotlinVersion>

    @get:OutputFile
    abstract val outputPath: RegularFileProperty

    init {
        group = "other"
        description = "Generates the all versions smoke-tests workflow file."
    }

    @TaskAction
    fun execute() {
        val outputPath = outputPath.get().asFile.toPath()

        outputPath.createParentDirectories()

        val workflow = getSmokeTestsWorkflow(supportedVersions.get())

        outputPath.writeText(workflow)
    }

    private fun getSmokeTestsWorkflow(versions: List<SupportedKotlinVersion>): String = $$"""
        name: Smoke Tests All versions

        on:
          schedule:
        #     Every Saturday at 2am EST
            - cron: '0 7 * * SAT'
          workflow_dispatch:
            inputs:
              versions:
                description: 'Select Kotlin versions to test (comma-separated)'
                required: true
                default: '$${versions.joinToString(",") { it.name.toString() }}'
              run_acceptance_tests:
                description: 'Run Acceptance Tests'
                type: boolean
                default: true
              run_type_mapping_tests:
                description: 'Run Type Mapping Tests'
                type: boolean
                default: true
              run_external_libraries_tests:
                description: 'Run External Libraries Tests'
                type: boolean
                default: true
              run_gradle_tests:
                description: 'Run Gradle Tests'
                type: boolean
                default: true

        env:
          ALL_KOTLIN_VERSIONS: '$${versions.joinToString(",") { it.name.toString() }}'

        permissions:
          contents: read
          checks: write

        concurrency:
          group: ci-smoke-tests-all

        jobs:
          compute-selected-kotlin-versions:
            name: Compute selected Kotlin versions
            runs-on: self-hosted
            outputs:
              kotlin-versions: ${{ steps.compute-selected-kotlin-versions.outputs.kotlin-versions }}
            steps:
              - name: Compute selected Kotlin versions
                id: compute-selected-kotlin-versions
                run: |
                  INPUTS="${{ inputs.versions || env.ALL_KOTLIN_VERSIONS }}"
                  JSON_ARRAY=$(echo "$INPUTS" | awk -F',' '{ for(i=1;i<=NF;i++) printf "\"%s\"%s", $i, (i<NF?",":"") }' | sed 's/.*/[&]/')
                  echo "kotlin-versions=$JSON_ARRAY" >> $GITHUB_OUTPUT

          acceptance-tests:
            name: Acceptance Tests
            runs-on: self-hosted
            needs: compute-selected-kotlin-versions
            if: github.event_name == 'schedule' || inputs.run_acceptance_tests == true
            strategy:
              fail-fast: false
              matrix:
                version: ${{ fromJson(needs.compute-selected-kotlin-versions.outputs.kotlin-versions) }}
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
                  arguments: ':acceptance-tests:functional:test -PversionSupport.kotlin.enabledVersions=${{ matrix.version }}'
                  build-root-directory: SKIE
                env:
                  KOTLIN_LINK_MODE: static
                  KOTLIN_BUILD_CONFIGURATION: debug

          type-mapping-tests:
            name: Type Mapping Tests
            runs-on: self-hosted
            needs: compute-selected-kotlin-versions
            if: github.event_name == 'schedule' || inputs.run_type_mapping_tests == true
            strategy:
              fail-fast: false
              matrix:
                version: ${{ fromJson(needs.compute-selected-kotlin-versions.outputs.kotlin-versions) }}
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
                  arguments: ':acceptance-tests:type-mapping:test -PversionSupport.kotlin.enabledVersions=${{ matrix.version }}'
                  build-root-directory: SKIE
                env:
                  KOTLIN_LINK_MODE: static
                  KOTLIN_TARGET: macos_arm64
                  KOTLIN_BUILD_CONFIGURATION: debug

          external-libraries-tests:
            name: External Libraries Tests
            runs-on: self-hosted
            needs: compute-selected-kotlin-versions
            if: github.event_name == 'schedule' || inputs.run_external_libraries_tests == true
            strategy:
              fail-fast: false
              matrix:
                version: ${{ fromJson(needs.compute-selected-kotlin-versions.outputs.kotlin-versions) }}
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
                  arguments: ':acceptance-tests:libraries:test -PversionSupport.kotlin.enabledVersions=${{ matrix.version }}'
                  build-root-directory: SKIE
                env:
                  KOTLIN_LINK_MODE: static
                  KOTLIN_BUILD_CONFIGURATION: debug

          gradle-tests:
            name: Gradle Tests
            runs-on: self-hosted
            needs: compute-selected-kotlin-versions
            if: github.event_name == 'schedule' || inputs.run_gradle_tests == true
            steps:
              - name: Checkout Repo
                uses: actions/checkout@v3
                with:
                  submodules: true
                  token: ${{ secrets.ACCEPTANCE_TESTS_TOKEN }}
              - name: Prepare Worker
                uses: ./.github/actions/prepare-worker
              - name: Run Gradle Tests
                uses: gradle/gradle-build-action@v2.4.2
                id: run-tests
                with:
                  arguments: >-
                    :test
                    -PtestLevel=smoke
                    -PtestType=gradle
                    "-Pmatrix.targets=macosArm64"
                    "-Pmatrix.configurations=debug"
                    "-Pmatrix.linkModes=static"
                  build-root-directory: test-runner
              - name: Publish Test Report
                uses: mikepenz/action-junit-report@v4
                if: ${{ failure() || success() }}
                with:
                  check_name: "Smoke Test Reports - Gradle Tests"
                  report_paths: 'test-runner/build/test-results/test/TEST-*.xml'
                  require_tests: true

        """.trimIndent()
}
