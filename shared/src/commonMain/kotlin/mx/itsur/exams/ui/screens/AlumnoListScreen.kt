package mx.itsur.exams.ui.screens
import mx.itsur.exams.util.currentTimeMs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel
import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.domain.model.Rol
import mx.itsur.exams.domain.usecase.generarId
import mx.itsur.exams.presentation.viewmodel.AlumnoListViewModel
import mx.itsur.exams.ui.components.*
import mx.itsur.exams.ui.theme.*

data class AlumnoListScreen(val admin: Alumno) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<AlumnoListViewModel>()
        val alumnos by viewModel.alumnos.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        var showForm by remember { mutableStateOf(false) }
        var deleteId by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) { viewModel.loadAlumnos() }

        Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
            ITSURHeader(
                title = "Alumnos",
                subtitle = "${alumnos.size} registrados",
                onBackClick = { navigator.pop() },
                actions = {
                    IconButton(onClick = { showForm = true }) {
                        Text("＋", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )

            if (isLoading) {
                LoadingIndicator()
            } else if (alumnos.isEmpty()) {
                EmptyState(mensaje = "No hay alumnos registrados.\nPresiona ＋ para agregar.", emoji = "👨‍🎓")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(alumnos, key = { it.id }) { alumno ->
                        AlumnoCard(alumno = alumno, onDelete = { deleteId = alumno.id })
                    }
                }
            }
        }

        if (showForm) {
            RegistrarAlumnoDialog(
                onDismiss = { showForm = false },
                onRegistrar = { a, pass ->
                    viewModel.registrar(a, pass)
                    showForm = false
                }
            )
        }

        deleteId?.let { id ->
            AlertDialog(
                onDismissRequest = { deleteId = null },
                title = { Text("¿Eliminar alumno?") },
                text = { Text("Esta acción eliminará al alumno del sistema.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.eliminar(id); deleteId = null }) {
                        Text("Eliminar", color = ITSURError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteId = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun AlumnoCard(alumno: Alumno, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(ITSURVerdeClaro, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumno.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                    fontWeight = FontWeight.Bold,
                    color = ITSURVerdeOscuro,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alumno.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(alumno.numeroControl, style = MaterialTheme.typography.bodyMedium, color = ITSURGrisMedio)
                Text(alumno.grupo, style = MaterialTheme.typography.labelSmall, color = ITSURVerde)
            }
            IconButton(onClick = onDelete) { Text("🗑", fontSize = 18.sp) }
        }
    }
}

@Composable
fun RegistrarAlumnoDialog(onDismiss: () -> Unit, onRegistrar: (Alumno, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var numeroControl by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Alumno", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ITSURTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre completo")
                ITSURTextField(value = email, onValueChange = { email = it }, label = "Correo institucional")
                ITSURTextField(value = numeroControl, onValueChange = { numeroControl = it }, label = "Número de control")
                ITSURTextField(value = grupo, onValueChange = { grupo = it }, label = "Grupo")
                ITSURTextField(value = password, onValueChange = { password = it }, label = "Contraseña inicial", isPassword = true)
            }
        },
        confirmButton = {
            ITSURButton(
                text = "Registrar",
                onClick = {
                    if (nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        onRegistrar(
                            Alumno(
                                id = generarId(),
                                nombre = nombre,
                                email = email,
                                numeroControl = numeroControl,
                                grupo = grupo,
                                rol = Rol.ALUMNO,
                                passwordHash = "",
                                fechaRegistro = currentTimeMs()
                            ),
                            password
                        )
                    }
                }
            )
        },
        dismissButton = {
            ITSURButton(text = "Cancelar", onClick = onDismiss, secondary = true)
        }
    )
}
