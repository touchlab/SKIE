# Installation

Make sure that your project uses Gradle 7.3 or higher and exactly the same Kotlin compiler version as SKIE (1.7.20).

Kotlin compiler plugins are generally not stable and break with almost every release. So before you do anything else, check that your project compiles (especially if you had to change the versions).

SKIE is deployed in a private Touchlab Maven repository. To access artifacts from that repository, you need to add the following code in `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        // Previously present repositories like mavenCentral()
        // ...
        maven("https://api.touchlab.dev/public")
    }
}

dependencyResolutionManagement {
    repositories {
        // Previously present repositories like mavenCentral()
        // ...
        maven("https://api.touchlab.dev/public")
    }
}
```

To enable SKIE, add the plugin in `build.gradle.kts` of the module that builds the native framework:

```kotlin
plugins {
    id("co.touchlab.skie") version "XXX"
}
```

You can find the most recent version of the plugin by looking at the [tags](https://github.com/touchlab/SKIE/tags) in this repository. The Maven repository does not have a browser at the time of writing.

SKIE should not be applied to other modules - it automatically works with all of the code in every module (including 3rd party dependencies). The Swift/Xcode side does not require any configuration changes.

That is the totality of what's required to integrate SKIE! Check out the [README](README.md) to see a list of features that SKIE supports. If you find that you want or need to customize SKIE's behavior, then check out the [Configuration doc](Configuration.md).
