let r0 = A<KotlinInt>(i: 0, k: 1).value
let r1 = A<KotlinInt>(k: 1).value
let r2 = A<KotlinInt>().value

if r0 == r1 && r1 == r2 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2)")
}
