import Foundation
import Kotlin

let a: A = A1()

switch a.exhaustively() {
    case .A1(_):
        exit(0)
    case .A2(_):
        exit(1)
}
