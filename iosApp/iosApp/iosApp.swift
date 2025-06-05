import SwiftUI
import ComposeApp
import FirebaseCore
import GoogleSignIn

// Estado observable para almacenar el deeplink pendiente
class DeepLinkState: ObservableObject {
    static let shared = DeepLinkState()
    @Published var pendingDeepLink: String?
}

class AppDelegate: NSObject, UIApplicationDelegate {

  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
      MainKt.initialise()
      NSLog("🟢 DEBUG: App inicializada")
      // FirebaseApp.configure() - Removed to prevent double initialization
      AppInitializer.shared.onApplicationStart()
      
      return true
  }
    
  // Este método es para iOS 13 y anteriores o enlaces que no son manejados por SwiftUI
  func application(
        _ app: UIApplication,
        open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]
      ) -> Bool {
        NSLog("🟢 DEBUG: URL recibida desde AppDelegate: %@", url.absoluteString)
        
        // Si es un esquema de Google, lo manejamos primero
        if url.scheme?.contains("com.googleusercontent.apps") == true {
            return GIDSignIn.sharedInstance.handle(url)
        }

        // Manejar deep links de Criticly
        if url.scheme == "criticly" {
            NSLog("🟢 DEBUG: Deep Link Criticly recibido en AppDelegate: %@", url.absoluteString)
            // Actualizamos el DeepLinkState para que SwiftUI pueda reaccionar
            DeepLinkState.shared.pendingDeepLink = url.absoluteString
            return true
        }

        return false
      }
}

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @StateObject private var deepLinkState = DeepLinkState.shared
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                // Este modificador captura todos los deep links que llegan a la app
                .onOpenURL { url in
                    NSLog("🟢 DEBUG: URL recibida en onOpenURL: %@", url.absoluteString)
                    
                    // Manejo específico para autenticación de Google
                    if url.scheme?.contains("com.googleusercontent.apps") == true {
                        GIDSignIn.sharedInstance.handle(url)
                        return
                    }
                    
                    // Manejo para deep links Criticly
                    if url.scheme == "criticly" {
                        NSLog("🟢 DEBUG: Deep Link Criticly recibido en onOpenURL: %@", url.absoluteString)
                        // Guardamos el deep link en el estado compartido
                        deepLinkState.pendingDeepLink = url.absoluteString
                    }
                }
                .onAppear {
                    NSLog("🟢 DEBUG: ContentView apareció")
                    if let pendingLink = deepLinkState.pendingDeepLink {
                        NSLog("🟢 DEBUG: Procesando deep link pendiente: %@", pendingLink)
                        // Pequeño delay para asegurar que la UI está lista
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            MainKt.processDeepLink(deepLink: pendingLink)
                            deepLinkState.pendingDeepLink = nil
                        }
                    }
                }
                .onChange(of: deepLinkState.pendingDeepLink) { newLink in
                    if let link = newLink {
                        NSLog("🟢 DEBUG: Deep link cambió a: %@", link)
                        // Pequeño delay para asegurar que la UI está lista
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            MainKt.processDeepLink(deepLink: link)
                            deepLinkState.pendingDeepLink = nil
                        }
                    }
                }
        }
    }
}
