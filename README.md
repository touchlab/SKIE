# SwiftGen

IMPORTANT: SwiftGen and other repositories from the SwiftKt project are not publicly released.

SwiftGen is a part of the SwiftKt project.
It is a compiler plugin that generates Swift code wrappers for Objective-C headers provided by the Kotlin compiler.
The goal is to improve interop between Kotlin and Swift.

The plugin internally depends on SwiftPack and SwiftLink (both from the SwiftKt project).
The SwiftKt project is under heavy development and is far from being stable.
Certainly do not use it in any production project.

SwiftGen currently supports the following features:

- Sealed class/interfaces (*)

(*) - These features are not fully implemented yet. Expect bugs and compilation errors.

The discussion about any part of the SwiftKt project happens in the `swiftkt` Slack channel.

## Installation

As the first step make sure that your project uses exactly the same Kotlin compiler version as this plugin.
(Kotlin compiler plugins are in general not stable and break with almost every release.)

At this point make sure that your project compiles before you do anything else (especially if you had to change the Kotlin compiler version).

The SwiftKt project is deployed in a private Touchlab's Maven repository.
To access artifacts from that repository, you need to add the following code in the `build.gradle.kts`:

```kotlin
repositories {
    maven("https://api.touchlab.dev/public")
}
```

Kotlin compiler plugins are configured by a Gradle plugin, so you also need to modify the `settings.gradle.kts` file and add:

```kotlin
pluginManagement {
    repositories {
        maven("https://api.touchlab.dev/public")
    }
}
```

To enable SwiftGen, register the following plugins in `build.gradle.kts`:

```kotlin
plugins {
    id("co.touchlab.swiftlink") version "XXX"
    id("co.touchlab.swiftpack") version "YYY"
    id("co.touchlab.swiftgen") version "ZZZ"
}
```

You can find the most recent version of each plugin by looking at the tags in the corresponding repository.
(The Maven repository does not have a browser at the time of writing.)

Note that `co.touchlab.swiftlink` is only necessary in the final module that builds the Kotlin framework.
Including it in other modules has no effect.
`co.touchlab.swiftgen` depends on `co.touchlab.swiftpack` so you always need to include both if you want to use SwiftGen.
However, you do not have to include them if you do not want to use SwiftGen in the given module.

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

func exhaustively(_ self: A) -> Enum {
    if let v = self as? A1 {
        return Enum.A1(v)
    } else if let v = self as? A2 {
        return Enum.A2(v)
    } else {
        fatalError("Unknown subtype. This error should not happen under normal circumstances since A is sealed.")
    }
}
```

The interop utilizes the fact that Swift supports exhaustive switch if used with Swift enums.
Therefore, we use the `exhaustively` function to wrap the Kotlin object in a Swift enum.
To simulate the smart-casting we use an enum with associated value.
Thanks to the above code you can write this:

```swift
switch exhaustively(a) {
case .A1(let a):
    return a.i
case .A2(let a):
    return a.k
}
```

If you do not need the smart-casting, you can write just this:

```swift
switch exhaustively(a) {
case .A1(_):
    print(a)
case .A2(_):
    fatalError()
}
```

