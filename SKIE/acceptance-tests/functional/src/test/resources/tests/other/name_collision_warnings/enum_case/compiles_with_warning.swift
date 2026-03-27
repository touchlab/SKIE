# SuccessWithWarning(w: 'static var Kotlin.__A.aaBb' was renamed to 'static var Kotlin.__A.aaBb_' because of a name collision with an another declaration 'static var Kotlin.__A.aaBb'. Consider resolving the conflict either by changing the name in Kotlin, or via the @ObjCName annotation. You can also suppress this warning using the 'SuppressSkieWarning.NameCollision' configuration. However using renamed declarations from Swift is not recommended because their name will change if the conflict is resolved.)

let a = A.aaBb
let b = A.aaBb_

exit(0)
