let a: A = A1()

switch onEnum(of: a) {
    case .X(_):
        exit(0)
    case .Other:
        exit(1)
}
