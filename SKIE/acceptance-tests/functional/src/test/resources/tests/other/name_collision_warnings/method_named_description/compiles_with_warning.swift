# SuccessWithWarning(w: 'func Kotlin.A.description() -> Swift.String' was renamed to 'func Kotlin.A.description_() -> Swift.String' because of a name collision with an another declaration 'func Kotlin.KotlinBase.description() -> Swift.String'. Consider resolving the conflict either by changing the name in Kotlin, or via the @ObjCName annotation. You can also suppress this warning using the 'SuppressSkieWarning.NameCollision' configuration. However using renamed declarations from Swift is not recommended because their name will change if the conflict is resolved.)

// TODO Change to this once CreateKirDescriptionAndHashPropertyPhase is enabled
// # SuccessWithWarning(w: 'func Kotlin.A.description() -> Swift.String' was renamed to 'func Kotlin.A.description_() -> Swift.String' because of a name collision with an another declaration 'var Kotlin.KotlinBase.description'. Consider resolving the conflict either by changing the name in Kotlin, or via the @ObjCName annotation. You can also suppress this warning using the 'SuppressSkieWarning.NameCollision' configuration. However using renamed declarations from Swift is not recommended because their name will change if the conflict is resolved.)

A().description_()

exit(0)
