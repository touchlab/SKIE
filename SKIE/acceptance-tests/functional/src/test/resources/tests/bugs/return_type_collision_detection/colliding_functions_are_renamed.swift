let rrA: A = Required.a1.renamed(i: 0)
let rrB: B = Required.a1.renamed_(i: 0)

let rnA: A = Required.a1.notRenamed(i: 0)
let rnC: C = Required.a1.notRenamed(i: 0)

let orA: A? = Optional.a1.renamed(i: 0)
let orB: B? = Optional.a1.renamed_(i: 0)

let onA: A? = Optional.a1.notRenamed(i: 0)
let onB: B? = Optional.a1.notRenamed(i: 0)

let gnA: T<A> = Generics.a1.notRenamed(i: 0)
let gnB: T<B> = Generics.a1.notRenamed(i: 0)

exit(0)
