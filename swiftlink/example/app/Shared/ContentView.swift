//
//  ContentView.swift
//  Shared
//
//  Created by Tadeas Kriz on 2022-05-13.
//

import SwiftUI
import ExampleKit

struct ContentView: View {
    let classWithTransformedMembers = ClassWithTransformedMembers()

    var body: some View {
        VStack(spacing: 16) {
            Text("Hello, world! - \(KotlinTest()) - \(String(describing: SwiftTest()))")

            Text("Renamed: \(Renamed()), \(classWithTransformedMembers.renamedProperty), \(classWithTransformedMembers.renamedMethod())")

            Text("Hidden: \(__ToBeHidden()), \(classWithTransformedMembers.__toBeHiddenProperty), \(classWithTransformedMembers.__toBeHiddenMethod())")


        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
