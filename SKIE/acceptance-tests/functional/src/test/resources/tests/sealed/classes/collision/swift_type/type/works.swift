let a: A = A.Type_()

switch onEnum(of: a) {
    case .a1(_):
        exit(1)
    case .type(_):
        exit(0)
}
