let a: A = A(i: 0, k: 1)

let r0 = a.doCopy(i: 2, k: 3).i
let r1 = a.doCopy(i: 2).i
let r2 = a.doCopy(k: 2).k
let r3 = a.doCopy().i + 2

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
