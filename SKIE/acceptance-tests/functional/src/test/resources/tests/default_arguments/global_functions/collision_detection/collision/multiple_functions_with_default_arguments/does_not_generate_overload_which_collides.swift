# SwiftCompilationError(error: 'foo(i:)' is unavailable in Swift)

let r0 = AKt.foo(i: 0)
let r1 = AKt.foo()

if r0 == r1 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1)")
}

