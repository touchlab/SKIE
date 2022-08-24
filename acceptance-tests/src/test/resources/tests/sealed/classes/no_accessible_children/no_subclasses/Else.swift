func test(a: A) {
    switch exhaustively(a) {
    case .Else:
        exit(1)
    }
}

exit(0)