let b = B()
let a: A = b
let i1: I1 = a
let i2: I2 = a


var result = b.get(value: 6).int32Value
result -= a.get(value: 3).int32Value

result -= (i1.get(value: 2 as KotlinInt) as! KotlinInt).int32Value
result -= (i2.get(value: 1 as KotlinInt) as! KotlinInt).int32Value

exit(result)
