import Foundation
import Kotlin

let a: A = A1()

switch onEnum(of: a) {
    case .A1(_):
        exit(0)
    case .A2(_):
        exit(1)
}


fatalError("Tested program ended without explicitly calling `exit(0)`.")
