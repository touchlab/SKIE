let a: A = A1A(value: KotlinInt(int: 0))

switch onEnum(of: a) {
    case .a1(let a):
        exit(0)
    case .a2(let a):
        exit(1)
}
