let a: A = A1()

let e = onEnum(of: a)

func requiresHashable(_ p: any Hashable) {
}

requiresHashable(e)

exit(0)
