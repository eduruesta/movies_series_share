import androidx.compose.ui.window.ComposeUIViewController
import com.bebi.app.App
import com.bebi.app.di.module.initKoin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.initialize

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}

fun initialise() {
    Firebase.initialize()
    Firebase.analytics.setAnalyticsCollectionEnabled(true)
    Firebase.database.setLoggingEnabled(true)
    Firebase.database.setPersistenceEnabled(true)
}