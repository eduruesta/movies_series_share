import androidx.compose.ui.window.ComposeUIViewController
import com.bebi.app.App
import com.bebi.app.di.module.initKoin
import platform.UIKit.UIViewController

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}