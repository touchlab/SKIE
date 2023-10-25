import Foundation
import Kotlin

@main
struct Main {
    static func main() async throws {
        let a: X = X<Int>()
        
        let c = Y<Int>()
        
        print(a)
        
        foo(a: StrReader(str: "ABC", file: "file", pos: 0))
    }
    
    static func foo(a: StrReader) {
        print(a.pos_)
    }
}

typealias X = Foo
typealias Y<C> = Foo<C>

class Foo<T> {
    
}
