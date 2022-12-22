# Skip

let result = await Task.detached {
    try! await A<KotlinInt>().foo(i: 0)
}.value

exit(result.int32Value)
