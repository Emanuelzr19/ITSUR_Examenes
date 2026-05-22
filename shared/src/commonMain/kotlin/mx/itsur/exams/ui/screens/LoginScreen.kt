package mx.itsur.exams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel
import mx.itsur.exams.domain.model.Rol
import mx.itsur.exams.presentation.viewmodel.LoginState
import mx.itsur.exams.presentation.viewmodel.LoginViewModel
import mx.itsur.exams.ui.components.ITSURButton
import mx.itsur.exams.ui.components.ITSURTextField
import mx.itsur.exams.ui.theme.*

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<LoginViewModel>()
        val state by viewModel.state.collectAsState()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        LaunchedEffect(state) {
            when (val s = state) {
                is LoginState.Success -> {
                    val alumno = s.alumno
                    viewModel.resetState()
                    if (alumno.rol == Rol.ADMIN) navigator.replace(DashboardAdminScreen(alumno))
                    else navigator.replace(DashboardAlumnoScreen(alumno))
                }
                is LoginState.Error -> errorMsg = s.message
                else -> {}
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ITSURVerdeOscuro, ITSURVerde, ITSURVerdeLima.copy(alpha = 0.3f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ITSURLogo(size = 110)

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "ITSUR",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Sistema de Exámenes",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(40.dp))

                Card(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ITSURTexto
                        )
                        Spacer(Modifier.height(24.dp))

                        ITSURTextField(
                            value = email,
                            onValueChange = { email = it; errorMsg = "" },
                            label = "Correo institucional"
                        )
                        Spacer(Modifier.height(14.dp))
                        ITSURTextField(
                            value = password,
                            onValueChange = { password = it; errorMsg = "" },
                            label = "Contraseña",
                            isPassword = true
                        )

                        if (errorMsg.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = errorMsg,
                                color = ITSURError,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        ITSURButton(
                            text = if (state is LoginState.Loading) "Ingresando..." else "Ingresar",
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = state !is LoginState.Loading && email.isNotBlank() && password.isNotBlank()
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Admin: admin@itsur.edu.mx / admin123",
                            style = MaterialTheme.typography.labelSmall,
                            color = ITSURGrisMedio,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
