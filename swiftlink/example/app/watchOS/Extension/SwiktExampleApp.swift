//
//  SwiktExampleApp.swift
//  SwiktExample_watchOS_Dynamic WatchKit Extension
//
//  Created by Tadeas Kriz on 2022-05-17.
//

import SwiftUI

@main
struct SwiktExampleApp: App {
    @SceneBuilder var body: some Scene {
        WindowGroup {
            NavigationView {
                ContentView()
            }
        }

        WKNotificationScene(controller: NotificationController.self, category: "myCategory")
    }
}
