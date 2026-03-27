let a: A<KotlinInt> = A1A(value: 0)

switch onEnum(of: a) {
    case .a1(let a):
        exit(a.value.int32Value)
    case .a2(let a):
        exit(1)
}
