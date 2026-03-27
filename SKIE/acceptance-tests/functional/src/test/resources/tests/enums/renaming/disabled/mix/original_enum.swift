let values = [__A.aaBbCc, __A.aaBb, __A.aaBbCc_]

switch values[Int(index)] as A {
case .aaBbCc:
    exit(0)
case .aaBb:
    exit(1)
case .aaBbCc_:
    exit(2)
}
