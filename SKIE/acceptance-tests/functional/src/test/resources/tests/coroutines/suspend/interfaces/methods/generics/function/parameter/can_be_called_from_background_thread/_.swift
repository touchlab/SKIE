let result = await Task.detached {
    try! await (A1() as A).foo(i: KotlinInt(1), k: 0)
}.value

exit(result.int32Value)
