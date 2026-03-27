let a: A? = nil

switch onEnum(of: a) {
    case .a1(_):
        exit(1)
    case .a2(_):
        exit(2)
    case .none:
        exit(0)
}
