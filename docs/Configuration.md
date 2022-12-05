# Configuration

SKIE is an opinionated plugin and makes choices that might not work for every use case, so it provides a way to change some of its default behavior. There are two different ways to change the configuration:

- locally - using Kotlin annotations
- globally - using Gradle extension provided by the SKIE Gradle plugin

## Local (via Kotlin annontation)

A local configuration change affects the behavior of a single declaration, which makes it suitable for suppressing the plugin if for example it does not work properly because of a bug.

The available annotations can be found in [the `:plugin:generator:configuration-annotations` module](https://github.com/touchlab/SKIE/tree/main/plugin/generator/configuration-annotations).

The following example changes the name of the `onEnum(of:)` function generated for `SealedKotlinClass` to `something(of:)`:

```kotlin
// SealedKotlinClass.kt

@SealedInterop.Function.Name("something")
sealed class SealedKotlinClass {
    ...
}
```

To use these annotations you need to add a dependency in `build.gradle.kts`:

```kotlin
dependencies {
    implementation("co.touchlab.skie:skie-generator-configuration-annotations:XXX")
}
```

These annotations can be used in any module that has access to that dependency - not just the one that applies the SKIE plugin.

## Global (via Gradle extension)

The global configuration can be applied to any class, including those from third party dependencies, which makes it a good place for changing SKIE's default behavior and providing configuration for classes that you cannot modify.

Global configurations are performed through a `skie` Gradle extension:

```kotlin
// build.gradle.kts
import co.touchlab.skie.configuration.gradle.SealedInterop

skie {
    configuration {
        group {
            SealedInterop.Function.Name("something")
        }
    }
}
```

The above example changes the name of the `onEnum(of:)` function to `something(of:)` for **all** sealed classes and interfaces. Note that you can add multiple options to a single `group { ... }`.

All of the available configuration options are listed in [the `:plugin:generator:configuration-gradle` module](https://github.com/touchlab/SKIE/tree/main/plugin/generator/configuration-gradle). Make sure that you import classes from package `co.touchlab.skie.configuration.gradle` (not `.annotations`).

The configuration can also be applied such that it affects only some declarations:

```kotlin
// build.gradle.kts

skie {
    configuration {
        group("co.touchlab.") {
            SealedInterop.Function.Name("something")
        }
    }
}
```

The configuration in the above example only applies to declarations from the `co.touchlab` package. The argument represents a prefix that is matched against the declaration's fully qualified name. You can target all declarations (by passing an empty string), everything in a package, just a single class, etc.

The `group` block can be called multiple times so that declarations can have different configurations. For example:

```kotlin
// build.gradle.kts

skie {
    configuration {
        group {
            SealedInterop.Function.Name("something")
        }
        group("co.touchlab.") {
            SealedInterop.Function.Name("somethingElse")
        }
    }
}
```

If multiple matching groups provide the same configuration key, then only the last one added will be implemented.

## Both Local and Global

Local and global configuration can be used at the same time, in which case the local configuration takes precedence. This behavior can be overridden like so:

```kotlin
// build.gradle.kts

skie {
    configuration {
        group(overridesAnnotations = true) {
            SealedInterop.Function.Name("something")
        }
    }
}
```

If the `overridesAnnotations` argument is set, then all keys in the group take precedence over the annotations. Keep in mind that the configuration can still be changed by another `group` block. Annotations can still be used to configure behavior not specified in the overriding group.

Configuration can be loaded from a file:

```kotlin
// build.gradle.kts

skie {
    configuration {
        from(File("config.json"))
    }
}
```

`group` and `from` can be freely mixed together and repeated multiple times. The file format is identical to [`acceptance-tests/src/test/resources/tests/config.json`](/acceptance-tests/src/test/resources/tests/config.json).
