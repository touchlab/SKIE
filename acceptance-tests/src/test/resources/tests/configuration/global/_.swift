let a: A = A1()
let b: B = B1()

switch exhaustively(a) {
    case .X(_):
        switch exhaustively(b) {
            case .X(_):
                exit(0)
        }
}
