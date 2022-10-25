let a: A = A2(value_A2: 1, value_A: 0)

switch onEnum(of: a) {
    case .A1(let a):
        switch onEnum(of: a) {
            case .A1A(let a):
                exit(1)
            case .A1B(let a):
                exit(1)
        }
    case .A2(let a):
        exit(a.value_A)
}
