let r0 = A(i: 0, k: 1, m: 2, o: 3).value
let r1 = A(i: 0, k: 1, m: 2).value
let r2 = A(i: 0, m: 2, o: 3).value
let r3 = A(i: 0, m: 2).value

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
