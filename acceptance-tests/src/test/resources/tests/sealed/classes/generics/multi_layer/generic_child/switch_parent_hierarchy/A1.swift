let a: A = A1A(value: KotlinInt(int: 0))

switch onEnum(of: a) {
    case .A1(let a):
        exit(0)
    case .A2(let a):
        exit(1)
}
