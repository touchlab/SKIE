# SwiftCompilationError(error: value of type 'C' has no member 'foo')

let c = C(value: 1)

c.foo = 1

let result = c.foo

exit(result)
