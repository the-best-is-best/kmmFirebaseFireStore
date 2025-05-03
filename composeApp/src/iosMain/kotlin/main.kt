import androidx.compose.ui.window.ComposeUIViewController
import io.gituhb.demo.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
