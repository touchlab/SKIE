let a: A = A.Any()

switch onEnum(of: a) {
    case .a1(_):
        exit(1)
    case .any(_):
        exit(0)
}
