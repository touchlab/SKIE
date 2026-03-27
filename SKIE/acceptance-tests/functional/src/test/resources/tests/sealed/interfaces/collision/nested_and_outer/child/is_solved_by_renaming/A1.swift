let a: A = A1()

switch onEnum(of: a) {
    case .a1(_):
        exit(0)
    case .X(_):
        exit(1)
}
