import SwiftUI
import ComposeApp
import FirebaseCore
import GoogleSignIn

class AppDelegate: NSObject, UIApplicationDelegate {

  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
      MainKt.initialise()
      // FirebaseApp.configure() - Removed to prevent double initialization
      AppInitializer.shared.onApplicationStart()
      
    return true
  }
    
    func application(
          _ app: UIApplication,
          open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]
        ) -> Bool {
          var handled: Bool

          handled = GIDSignIn.sharedInstance.handle(url)
          if handled {
            return true
          }

          // Manejar deep links de Criticly
          if url.scheme == "criticly" {
              print("Deep Link recibido: \(url.absoluteString)")
              // Pasamos la URL completa a nuestra aplicación Kotlin
              MainKt.processDeepLink(deepLink: url.absoluteString)
              return true
          }

          // Handle other custom URL types.

          // If not handled by this app, return false.
          return false
        }

    
}

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL(perform: { url in
                            GIDSignIn.sharedInstance.handle(url)
                        })
        }
    }
}
