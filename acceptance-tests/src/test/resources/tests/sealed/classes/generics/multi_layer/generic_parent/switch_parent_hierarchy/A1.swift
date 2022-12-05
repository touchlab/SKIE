let a: A<KotlinInt> = A1A(value: 0)

switch onEnum(of: a) {
    case .A1(let a):
        exit(a.value.int32Value)
    case .A2(let a):
        exit(1)
}
