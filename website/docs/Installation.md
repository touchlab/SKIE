---
sidebar_position: 2
---

# Installation

Installing SKIE involves small additions to just two gradle files. The Swift/Xcode side does not require any configuration changes.

Kotlin compiler plugins are generally not stable and break with almost every release, so please make sure that your project uses exactly the same Kotlin compiler version as SKIE (1.7.20) and Gradle 7.3 or higher.

**Before you do anything else, check that your project compiles (especially if you had to change Gradle and/or Kotlin versions).**

SKIE is deployed in a private Touchlab Maven repository. To access artifacts from that repository, you will need an API key from Touchlab, which we will happily provide upon request. If you do not know how to contact us, you can [do so here](https://touchlab.co/contact-us/).

Once you have your API key, you will then need to add the following code in `settings.gradle.kts` with your API key substituting for "YOUR_API_KEY_HERE":

```kotlin
pluginManagement {
    repositories {
        // Previously present repositories like mavenCentral()
        // ...
        maven("https://api.touchlab.dev/mvn/YOUR_API_KEY_HERE")
    }
}

dependencyResolutionManagement {
    repositories {
        // Previously present repositories like mavenCentral()
        // ...
        maven("https://api.touchlab.dev/mvn/YOUR_API_KEY_HERE")
    }
}
```

To enable SKIE, add the plugin in `build.gradle.kts` of the module that builds the native framework:

```kotlin
plugins {
    id("co.touchlab.skie") version "{{LATEST_GITHUB_VERSION}}"
}
```

SKIE should not be applied to other modules - it automatically works with all of the code in every module (including 3rd party dependencies).

That is the totality of what's required to integrate SKIE! Below is a list of features that SKIE supports. If you find that you want or need to customize SKIE's behavior, then check out the [Configuration doc](Configuration.md).
