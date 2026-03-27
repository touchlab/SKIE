let a: A = A1()
let b: B = B1()

switch onEnum(of: a) {
    case .A1(_):
        switch onEnum(of: b) {
            case .X(_):
                exit(0)
        }
}
