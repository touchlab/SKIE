let a: A = A1()
let b: B = B1()

switch exhaustively(a) {
    case .A1(_):
        switch exhaustively(b) {
            case .Y(_):
                exit(0)
        }
}
