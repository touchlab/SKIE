let result = await Task.detached {
    try! await skie(A<KotlinInt>()).foo(i: 0)
}.value

exit(result.int32Value)
