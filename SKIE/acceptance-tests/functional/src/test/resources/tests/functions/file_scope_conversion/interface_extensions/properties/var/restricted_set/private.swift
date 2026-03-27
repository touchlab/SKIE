# RuntimeError('+[KotlinAKt setPrivateFoo:value:]: unrecognized selector sent to class)

// Compiler bug - header contains a non-existent setter function

let c = C(value: 1)

c.privateFoo = 1
