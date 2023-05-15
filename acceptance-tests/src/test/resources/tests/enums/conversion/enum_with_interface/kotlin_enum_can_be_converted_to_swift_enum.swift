let a = A.a0.toKotlinEnum()

switch a.toSwiftEnum() {
case .a0:
    exit(0)
case .a1:
    exit(1)
}
