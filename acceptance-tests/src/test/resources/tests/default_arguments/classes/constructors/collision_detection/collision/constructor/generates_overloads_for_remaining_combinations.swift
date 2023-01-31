let r0 = A(i: 0, k: 1).value
let r1 = A(k: 1).value

if r0 == r1 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1)")
}
