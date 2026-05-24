package mx.itsur.exams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import mx.itsur.exams.ui.screens.LoginScreen
import mx.itsur.exams.ui.theme.ITSURTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ITSURTheme {
                    Navigator(screen = LoginScreen())
            }
        }
    }
}
