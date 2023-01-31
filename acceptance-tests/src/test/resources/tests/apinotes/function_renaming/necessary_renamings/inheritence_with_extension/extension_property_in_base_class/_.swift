let a1 = A1()

let zero = a1.foo
let one = a1.foo_

a1.foo = 2
a1.foo_ = 3

let two = a1.foo
let three = a1.foo_

exit(three - (two + one) - zero)
