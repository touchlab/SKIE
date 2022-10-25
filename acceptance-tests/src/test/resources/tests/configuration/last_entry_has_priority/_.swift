let a: A = A1()
let b: B = B1()

switch onEnum(of: a) {
    case .A1(_):
        switch onEnum(of: b) {
            case .Y(_):
                exit(0)
        }
}
