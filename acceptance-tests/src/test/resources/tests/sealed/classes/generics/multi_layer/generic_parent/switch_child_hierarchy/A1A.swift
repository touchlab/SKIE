let a: A1 = A1A(value: 0)

switch onEnum(of: a) {
    case .A1A(let a):
        exit(a.value.int32Value)
    case .A1B(let a):
        exit(1)
}
