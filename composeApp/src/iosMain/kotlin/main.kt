import androidx.compose.ui.window.ComposeUIViewController
import com.bebi.app.App
import com.bebi.app.di.module.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}