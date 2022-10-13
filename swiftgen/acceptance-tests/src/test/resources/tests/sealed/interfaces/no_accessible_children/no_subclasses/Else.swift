func test(a: A) {
    switch onEnum(of: a) {
    case .Else:
        exit(1)
    }
}

exit(0)