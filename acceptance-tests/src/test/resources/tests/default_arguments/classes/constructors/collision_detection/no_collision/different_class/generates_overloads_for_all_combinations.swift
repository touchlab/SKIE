let r0 = A(i: 0, k: 1).value
let r1 = A(i: 0).value
let r2 = A(k: 1).value
let r3 = A().value

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
