# SwiftCompilationError(error: argument type 'Skie.co_touchlab_skie__kotlin.A.__Sealed' does not conform to expected type 'Hashable')

let a: A = A1()

let e = onEnum(of: a)

func requiresHashable(_ p: any Hashable) {
}

requiresHashable(e)

exit(0)
