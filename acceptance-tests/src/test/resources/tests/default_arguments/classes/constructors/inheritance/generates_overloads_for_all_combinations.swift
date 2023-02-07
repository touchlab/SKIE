let r0 = A(i: 1, k: 1).value
let r1 = A(i: 1).value
let r2 = B(i: 0).value

if r0 == r1 && r1 == r2 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2)")
}
