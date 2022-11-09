# SKIE

IMPORTANT: SKIE is not publicly released.

SKIE is a compiler plugin whose goal is to improve interop between Kotlin and Swift.

SKIE is under heavy development and is far from being stable.
Certainly do not use it in any production project.

SKIE currently supports the following features:

- Sealed class/interfaces
- Exhaustive enums
- Default arguments

These features are under active development and requires extra opt in (see section about "Experimental features"):

-

The discussion about SKIE project happens in the `skie` and `skie-pm` Touchlab Slack channels.

## Installation

As the first step make sure that your project uses exactly the same Kotlin compiler version as this plugin (1.7.20).
(Kotlin compiler plugins are in general not stable and break with almost every release.)
Next, ensure that you are using Gradle 7.3 or higher.

At this point check that your project compiles before you do anything else (especially if you had to change the versions).

The SKIE project is deployed in a private Touchlab's Maven repository.
To access artifacts from that repository, you need to add the following code in the `settings.gradle.kts` file:

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

You can find the most recent version of the plugin by looking at the tags in this repository.
(The Maven repository does not have a browser at the time of writing.)

The plugin should not be applied to other modules - SKIE automatically works with all code from all modules (included 3rd party dependencies).

The Swift/Xcode side does not require any configuration changes.

## Supported features

### Sealed classes/interfaces

This feature allows to exhaustively switch on sealed Kotlin hierarchies from Swift.
For example, consider the following Kotlin code:

```kotlin
sealed interface A

class A1(val i: Int) : A
class A2(val k: Int) : A
```

In Kotlin you can write this:

```kotlin
when (a) {
    is A1 -> a.i
    is A2 -> a.k
}
```

In the above example, the compiler ensures that the `when` expression lists all possible cases (i.e. that it is exhaustive).
Not only that, but the compiler also smart-casts the `a` expression to the correct type.
The smart-cast allows the developer to access the inner properties of each type without an additional cast.

To support this feature in Swift, we generate code that can be reduced to this:

```swift
enum Enum {
    case A1(A1)
    case A2(A2)
}

func onEnum(of sealed: A) -> Enum {
    if let sealed = sealed as? A1 {
        return Enum.A1(sealed)
    } else if let sealed = sealed as? A2 {
        return Enum.A2(sealed)
    } else {
        fatalError("Unknown subtype. This error should not happen under normal circumstances since A is sealed.")
    }
}
```

The interop utilizes the fact that Swift supports exhaustive switch if used with Swift enums.
Therefore, we use the `onEnum(of:)` function to wrap the Kotlin object in a Swift enum.
To simulate the smart-casting we use an enum with associated value.
Thanks to the above code you can write this:

```swift
switch onEnum(of: a) {
case .A1(let a):
    return a.i
case .A2(let a):
    return a.k
}
```

If you do not need the smart-casting, you can write just this:

```swift
switch onEnum(of: a) {
case .A1:
    print(a)
case .A2:
    fatalError()
}
```

### Exhaustive enums

Kotlin compiler exposes Kotlin enums as regular Objc classes (albeit with restricted subclassing).
As a result Swift code cannot leverage some features of enums, mainly exhaustive switching.

SKIE returns back this functionality by generating a Swift version of the given enum.
The Swift enum is accessible without writing any extra code (like the `onEnum` in the case of sealed classes).
This is possible because SKIE generates a so-called bridging header that tells the Swift compiler how to do the conversion automatically.

As an example consider following Kotlin code:

```kotlin
enum class A {
    A1, A2
}
```

Without SKIE you can still use `switch` from Swift code, like this:

```swift
switch (a) {
case .a1: print("A1")
case .a2: print("A2")
default: print("Unknown")
}
```

Note that the `default` case is required.

After applying SKIE plugin, the `default` case is no longer necessary.
Instead, you will see a compiler warning similar to this one:

```
warning: default will never be executed
default: print("Unknown")
```

### Default arguments

Both Kotlin and Swift have a feature called default arguments.
This feature allows the function caller to omit some function arguments.
The missing arguments are provided by the called function.

The problem with this feature is that Objc does not support default arguments in any way.
Therefore, Swift code must always call Kotlin functions with all arguments.

Adding the default arguments back is not that straightforward even-though both Kotlin and Swift support them.
The reason is that both languages implement this feature differently and therefore have different semantics.

For example, Kotlin default arguments can access the values of previous function parameters as well as `this` expression.
However, Swift default arguments can access only expression from global scope.

As a result, SKIE cannot just generate Swift functions with default arguments (at least not in all cases).
To solve this issue, generates Kotlin overloads of the given functions to match all possible ways to call that functions.

For example, let's take a data class:

```kotlin
data class A(val i: Int, val k: Int)
```

Data classes in Kotlin have an automatically generated method `copy`.
This method is used to create a new instance of the data class with some values modified.
For our example `A` the method can be written in the following way:

```kotlin
fun A.copy(i: Int = this.i, k: Int = this.k) = A(i, k)
```

Without SKIE the `copy` method is exposed to Swift under the following signature: `A.doCopy(i:k:)`.
(The renaming is necessary because of collision with Objc method `copy`.)
Since the Swift code cannot use Kotlin default arguments, all parameters must be provided - defeating the `copy` method purpose.

SKIE generates additional Kotlin overloads, that are visible from Swift under the following signatures:

- `A.doCopy()`
- `A.doCopy(i:)`
- `A.doCopy(k:)`

These overloads allow the Swift code to call the `copy` method as if the default arguments were directly supported.

While this approach is completely transparent from the Swift code, it has some drawbacks:

- It does not support interface methods (all other types of functions are supported, including interface extensions)
- Generated overloads may cause overload resolution conflicts.
- The number of generated overloads is `O(2^n)` where `n` is the number of default arguments (not all parameters).

SKIE tries to not generate functions that would cause conflicts, however, the implementation is not complete yet.
Namely, it does not properly handle inheritance, generics and generated overloads of multiple functions with default arguments.
If you run into this issue, you might have to disable the code generation for one of the functions (see section about Configuration).
Alternatively, you can rename one those functions (or their parameters).

As for the number of generated overloads: Since it is not possible to generate exponential number of functions, there is a limit to how many default arguments are supported.
If that number is exceeded, no extra functions are generated.

By default, the limit is set to 5, meaning at most 31 additional functions will be generated per function with default arguments.
This number was chosen based on internal experiments.
So far it seems to be a good trade-off between the number of supported cases (very few functions exceed that number) and the introduced overhead in compilation time and binary size.
However, that number might change in the future as we test the plugin on larger project.

The limit can be explicitly configured using the `DefaultArgumentInterop.MaximumDefaultArgumentCount` key/annotation (see section about Configuration).

Note that all of the above-mentioned problems might be mitigated in the future versions of SKIE.

## Configuration

SKIE plugin makes some opinionated choices that might not work for every use case.
To solve this issue, the plugin provides a way to change some of its default behavior.
There are two different ways to change the configuration:

- locally - using Kotlin annotations
- globally - using Gradle extension provided by the SKIE Gradle plugin

### Annotations

The local configuration changes the behavior only for a single declaration.
This makes it for example suitable for suppressing the plugin if it does not work properly because of some bug.
The available annotations can be found in the `:plugin:generator:configuration-annotations` module.

The following example changes the name of the `onEnum(of:)` function generated for the sealed class interop to `something(of:)`:

```kotlin
// A.kt

@SealedInterop.Function.Name("something")
sealed class A {
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

### Gradle

The global configuration can be applied to any class including those from 3rd party dependencies.
This ability makes it a good place for changing the plugin default behavior and to provide configuration for classes that you cannot modify.
The configuration is performed through a `skie` Gradle extension:

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

The above example changes the name of the `onEnum(of:)` function to `something(of:)` for all sealed classes/interfaces.
All the available configuration options are listed in the `:plugin:generator:configuration-gradle` module.
Note that you can add multiple options to a single group.

Make sure that you are importing classes from package "co.touchlab.skie.configuration.gradle" (not ".annotations").

The configuration can be applied such that it affects only some declarations:

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

In the above example the configuration is applied only to declarations from the `co.touchlab` package.
The argument represents a prefix that is matched against the declaration's fully qualified name.
You can target all declarations (by passing an empty string), everything in a package, just a single class, etc.

The `group` block can be called multiple times so that declarations can have different configurations.
For example:

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

If multiple matching groups provide the same configuration key, then the last one added wins.

The local and global configuration can be used at the same time in which case the local configuration takes precedence.
This behavior can be overridden:

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

If the `overridesAnnotations` argument is set, then all keys in the group take precedence over the annotations.
The configuration can still be changed by another `group` block.
Annotations can still be used to configure behavior not specified in the overriding group.

Configuration can be loaded from a file:

```kotlin
// build.gradle.kts

skie {
    configuration {
        from(File("config.json"))
    }
}
```

`group` and `from` can be freely mixed together and repeated multiple times.
The file format is identical to `build/co.touchlab.skie/config.json` which contains a JSON encoding all applied configuration.

## Experimental features

Experimental features are not fully implemented yet and have a high risk of introducing compilation errors.

Experimental features can be enabled using Gradle configuration via `ExperimentalFeatures.Enabled(true)`.
There are also the `ExperimentalFeatures.Enabled` and `ExperimentalFeatures.Disabled` annotations.
Some features may require additional configuration to work properly.
