let a: A = A1()
let b: B = B2()

switch onEnum(of: a) {
    case .a1(_):
        switch onEnum(of: b) {
            case .b1(_):
                exit(1)
            case .b2(_):
                exit(0)
        }
    case .a2(_):
        exit(1)
}
