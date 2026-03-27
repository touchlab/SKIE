let a: A = A2(k: 0)

switch onEnum(of: a) {
    case .else:
        exit(1)
    case .a2(let a2):
        exit(a2.k)
}
