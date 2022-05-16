//
//  ContentView.swift
//  Shared
//
//  Created by Tadeas Kriz on 2022-05-13.
//

import SwiftUI
import ExampleKit

struct ContentView: View {
    var body: some View {
        Text("Hello, world! - \(KotlinTest()) - \(String(describing: SwiftTest()))")
            .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
