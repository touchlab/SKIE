# kotlin-gradle-plugin-template 🐘

[![Use this template](https://img.shields.io/badge/-Use%20this%20template-brightgreen)](https://github.com/cortinico/kotlin-gradle-plugin-template/generate) [![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22)  [![License](https://img.shields.io/github/license/cortinico/kotlin-android-template.svg)](LICENSE) ![Language](https://img.shields.io/github/languages/top/cortinico/kotlin-android-template?color=blue&logo=kotlin)

A simple Github template that lets you create a **Gradle Plugin** 🐘 project using **100% Kotlin** and be up and running in a **few seconds**.

This template is focused on delivering a project with **static analysis** and **continuous integration** already in place.

## How to use 👣

Just click on [![Use this template](https://img.shields.io/badge/-Use%20this%20template-brightgreen)](https://github.com/cortinico/kotlin-gradle-plugin-template/generate) button to create a new repo starting from this template.

Once created don't forget to update the:
- [Plugin Coordinates](plugin-build/buildSrc/src/main/java/Coordinates.kt)
- Plugin Usages (search for [com.ncorti.kotlin.gradle.template](https://github.com/cortinico/kotlin-gradle-plugin-template/search?q=com.ncorti.kotlin.gradle.template&unscoped_q=com.ncorti.kotlin.gradle.template) in the repo and replace it with your ID).

## Features 🎨

- **100% Kotlin-only template**.
- Plugin build setup with **composite build**.
- 100% Gradle Kotlin DSL setup.
- Dependency versions managed via Gradle Versions Catalog (`libs.versions.toml`).
- CI Setup with GitHub Actions.
- Kotlin Static Analysis via `ktlint` and `detekt`.
- Publishing-ready to Gradle Portal.
- Issues Template (bug report + feature request)
- Pull Request Template.

## Composite Build 📦

This template is using a [Gradle composite build](https://docs.gradle.org/current/userguide/composite_builds.html) to build, test and publish the plugin. This means that you don't need to run Gradle twice to test the changes on your Gradle plugin (no more `publishToMavenLocal` tricks or so).

The included build is inside the [plugin-build](plugin-build) folder.

### `preMerge` task

A `preMerge` task on the top level build is already provided in the template. This allows you to run all the `check` tasks both in the top level and in the included build.

You can easily invoke it with:

```
./gradlew preMerge
```

If you need to invoke a task inside the included build with:

```
./gradlew -p plugin-build <task-name>
```


### Dependency substitution

Please note that the project relies on module name/group in order for [dependency substitution](https://docs.gradle.org/current/userguide/resolution_rules.html#sec:dependency_substitution_rules) to work properly. If you change only the plugin ID everything will work as expected. If you change module name/group, things might break and you probably have to specify a [substitution rule](https://docs.gradle.org/current/userguide/resolution_rules.html#sub:project_to_module_substitution).


## Publishing 🚀

This template is ready to let you publish to [Gradle Portal](https://plugins.gradle.org/).

The [![Publish Plugin to Portal](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Publish%20Plugin%20to%20Portal/badge.svg?branch=1.0.0)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Publish+Plugin+to+Portal%22) Github Action will take care of the publishing whenever you **push a tag**.

Please note that you need to configure two secrets: `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET` with the credetials you can get from your profile on the Gradle Portal.

## 100% Kotlin 🅺

This template is designed to use Kotlin everywhere. The build files are written using [**Gradle Kotlin DSL**](https://docs.gradle.org/current/userguide/kotlin_dsl.html) as well as the [Plugin DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block) to setup the build.

Dependencies are centralized inside the [libs.versions.toml](gradle/libs.versions.toml).

Moreover, a minimalistic Gradle Plugin is already provided in Kotlin to let you easily start developing your own around it.

## Static Analysis 🔍

This template is using [**ktlint**](https://github.com/pinterest/ktlint) with the [ktlint-gradle](https://github.com/jlleitschuh/ktlint-gradle) plugin to format your code. To reformat all the source code as well as the buildscript you can run the `ktlintFormat` gradle task.

This template is also using [**detekt**](https://github.com/arturbosch/detekt) to analyze the source code, with the configuration that is stored in the [detekt.yml](config/detekt/detekt.yml) file (the file has been generated with the `detektGenerateConfig` task).

## CI ⚙️

This template is using [**GitHub Actions**](https://github.com/cortinico/kotlin-android-template/actions) as CI. You don't need to setup any external service and you should have a running CI once you start using this template.

There are currently the following workflows available:
- [Validate Gradle Wrapper](.github/workflows/gradle-wrapper-validation.yml) - Will check that the gradle wrapper has a valid checksum
- [Pre Merge Checks](.github/workflows/pre-merge.yaml) - Will run the `preMerge` tasks as well as trying to run the Gradle plugin.
- [Publish to Plugin Portal](.github/workflows/publish-plugin.yaml) - Will run the `publishPlugin` task when pushing a new tag.

## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This template is licensed under the MIT License - see the [License](License) file for details.
Please note that the generated template is offering to start with a MIT license but you can change it to whatever you wish, as long as you attribute under the MIT terms that you're using the template.
