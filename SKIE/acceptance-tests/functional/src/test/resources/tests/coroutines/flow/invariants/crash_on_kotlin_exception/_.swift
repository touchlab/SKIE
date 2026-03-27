# RuntimeError(Uncaught Kotlin exception: kotlin.IllegalStateException: Exception from Kotlin)

let flow = AKt.foo()

for await i in flow {
    if i != 1 {
        exit(1)
    }
}

exit(2)
