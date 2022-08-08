let a: A = A1()
let b: B = B2()

switch exhaustively(a) {
    case .A1(_):
        switch exhaustively(b) {
            case .B1(_):
                exit(1)
            case .B2(_):
                exit(0)
        }
    case .A2(_):
        exit(1)
}
