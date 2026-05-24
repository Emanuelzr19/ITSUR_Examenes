package mx.itsur.exams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import cafe.adriel.voyager.navigator.Navigator
import mx.itsur.exams.ui.screens.LoginScreen
import mx.itsur.exams.ui.theme.ITSURTheme
import mx.itsur.exams.util.AndroidContextProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidContextProvider.init(this)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            ITSURTheme {
                Navigator(screen = LoginScreen())
            }
        }
    }
}
