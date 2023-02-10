package tests.bugs.default_arguments_collision_in_larger_hierarchy

open class A(val i: Int)

open class A1(i: Int, val k: Int = 0) : A(i)

class A2 : A1(0, 1)
