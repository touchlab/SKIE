---
sidebar_position: 3
title: Known issues
---

# Known issues

## Classes from Foundation framework are not in scope after adding SKIE

Frameworks produced by Kotlin/Native transitively export Foundation framework by default.
That means that if you import a Kotlin framework in a Swift file, you can use classes from Foundation even without explicitly importing Foundation.

However, frameworks produced by SKIE do not transitively export Foundation framework because that can cause name conflicts between Kotlin classes and Foundation classes.
These name collisions can only happen in frameworks that contain some Swift code - like the ones produced by SKIE.
Which is why, the Kotlin compiler can use the transitive export but SKIE can't.

The solution is to explicitly import Foundation framework in all files that import the Kotlin framework.
This can be done quickly by finding all occurrences of `import YourKotlinModuleName` and replacing them with:

```
import YourKotlinModuleName
import Foundation
```
