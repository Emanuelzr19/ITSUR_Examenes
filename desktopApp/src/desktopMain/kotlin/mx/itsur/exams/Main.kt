package mx.itsur.exams

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import mx.itsur.exams.data.local.DatabaseDriverFactory
import mx.itsur.exams.di.sharedModule
import mx.itsur.exams.ui.screens.LoginScreen
import mx.itsur.exams.ui.theme.ITSURTheme
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    startKoin {
        modules(
            module { single { DatabaseDriverFactory() } },
            sharedModule
        )
    }

    application {
        val windowState = rememberWindowState(width = 1100.dp, height = 780.dp)
        Window(
            onCloseRequest = ::exitApplication,
            title = "ITSUR — Sistema de Exámenes",
            state = windowState
        ) {
            ITSURTheme {
                Navigator(screen = LoginScreen())
            }
        }
    }
}
