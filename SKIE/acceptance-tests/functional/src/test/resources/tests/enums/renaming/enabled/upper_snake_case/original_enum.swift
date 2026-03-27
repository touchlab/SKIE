let values = [__A.aa, __A.aaBb, __A.aaBbCc]

switch values[Int(index)] as A {
case .aa:
    exit(0)
case .aaBb:
    exit(1)
case .aaBbCc:
    exit(2)
}
