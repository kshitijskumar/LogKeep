import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        LogKeepIos.shared.start(
            config: LogKeepConfig(isEnabled: true, maxEntriesPerSession: 1000, maxSessions: 5)
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
