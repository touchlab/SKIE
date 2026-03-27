# KotlinLinkingError(error: cannot convert return expression of type '(any GenericBound)?' to return type 'GenericImpl?')
// TODO[SKIE-551]: Remove the above line after generating custom framework header

let a = A.a1
let aImplementedReturn: GenericImpl = a.implementMe()
let aDefaultReturn: GenericBound? = a.default()

let b = B()
let bImplementedReturn: GenericImpl = b.implementMe()
let bDefaultReturn: GenericBound? = b.default()

let c = C()
let cImplementedReturn: GenericImpl = b.implementMe()
let cDefaultReturn: GenericImpl? = b.default()

exit(0)
