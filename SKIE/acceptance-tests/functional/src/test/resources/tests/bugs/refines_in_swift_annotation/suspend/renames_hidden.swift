let r1 = (try! await __foo()).int32Value
let r2 = (try! await AKt.__foo()).int32Value
let r3 = (try! await AKt.____foo()).int32Value

exit(r1 + r2 + r3)
