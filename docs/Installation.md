# Installation

Make sure that your project uses Gradle 7.3 or higher and exactly the same Kotlin compiler version as SKIE (1.7.20).

Kotlin compiler plugins are generally not stable and break with almost every release. So before you do anything else, check that your project compiles (especially if you had to change the versions).

SKIE is deployed in a private Touchlab Maven repository. To access artifacts from that repository, you will need an API key from Touchlab, which we will happily provide upon request. If you do not know how to contact us, you can either [do so here](https://touchlab.co/contact-us/) or in the `#touchlab-tools` channel of the Kotlin Community Slack. To join the Kotlin Community Slack, [request access here](http://slack.kotlinlang.org/).

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
    id("co.touchlab.skie") version "XXX"
}
```

You can find the most recent version of the plugin by looking at the [tags](https://github.com/touchlab/SKIE/tags) in this repository. The Maven repository does not have a browser at the time of writing.

SKIE should not be applied to other modules - it automatically works with all of the code in every module (including 3rd party dependencies). The Swift/Xcode side does not require any configuration changes.

That is the totality of what's required to integrate SKIE! Check out the [README](README.md) to see a list of features that SKIE supports. If you find that you want or need to customize SKIE's behavior, then check out the [Configuration doc](Configuration.md).
