# Skip
# SwiftCompilationError(error: cannot convert value of type 'String' to expected argument type 'Int32')

try! await A<KotlinInt, KotlinInt>().foo(i: KotlinInt(1), k: "B")
