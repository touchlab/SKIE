func test(a: A) {
    switch onEnum(of: a) {
    case .else:
        exit(1)
    }
}

exit(0)
