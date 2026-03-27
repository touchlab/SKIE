let values = [__A.aa, __A.aabb, __A.aabbcc]

switch values[Int(index)] as A {
case .aa:
    exit(0)
case .aabb:
    exit(1)
case .aabbcc:
    exit(2)
}
