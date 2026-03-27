A.a1.foo(completionHandler: { result, error in
    exit(result?.int32Value ?? 1)
})
