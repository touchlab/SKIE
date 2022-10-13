let a: A = A2(k: 0)

switch onEnum(of: a) {
    case .Other:
        exit(1)
    case .A2(let a2):
        exit(a2.k)
}
