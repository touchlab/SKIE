let a: A = A2(value_A2: 0, value_A: 1)

switch onEnum(of: a) {
    case .a1(let a):
        switch onEnum(of: a) {
            case .a1A(let a):
                exit(1)
            case .a1B(let a):
                exit(1)
        }
    case .a2(let a):
        exit(a.value_A2)
}
