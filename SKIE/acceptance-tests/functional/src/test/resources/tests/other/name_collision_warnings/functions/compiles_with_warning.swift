# SuccessWithWarning(w: 'static func Kotlin.AKt.foo(i: Swift.Int32) -> Swift.Void' was renamed to 'static func Kotlin.AKt.foo_(i: Swift.Int32) -> Swift.Void' because of a name collision with an another declaration 'static func Kotlin.AKt.foo(i: Swift.Int32) -> Swift.Void'. Consider resolving the conflict either by changing the name in Kotlin, or via the @ObjCName annotation. You can also suppress this warning using the 'SuppressSkieWarning.NameCollision' configuration. However using renamed declarations from Swift is not recommended because their name will change if the conflict is resolved.)

AKt.foo(i: 0)
AKt.foo_(i: 0)

exit(0)
