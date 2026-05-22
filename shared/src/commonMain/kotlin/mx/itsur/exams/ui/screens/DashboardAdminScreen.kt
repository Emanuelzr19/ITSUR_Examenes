package mx.itsur.exams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.ui.theme.*

data class DashboardAdminScreen(val admin: Alumno) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(ITSURVerdeOscuro, ITSURVerde)))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ITSUR — Exámenes", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("Panel Administrativo", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Bienvenido, ${admin.nombre.split(" ").first()}", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                    ITSURLogo(size = 56)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("¿Qué deseas hacer?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ITSURTexto)

                AdminMenuCard(
                    titulo = "Gestión de Exámenes",
                    descripcion = "Crear, editar y eliminar exámenes",
                    emoji = "📝",
                    color = ITSURVerdeClaro,
                    onClick = { navigator.push(ExamenListAdminScreen(admin)) }
                )
                AdminMenuCard(
                    titulo = "Gestión de Alumnos",
                    descripcion = "Registrar y administrar alumnos",
                    emoji = "👨‍🎓",
                    color = Color(0xFFE8F5E9),
                    onClick = { navigator.push(AlumnoListScreen(admin)) }
                )
                AdminMenuCard(
                    titulo = "Reportes y Calificaciones",
                    descripcion = "Ver resultados por examen o alumno",
                    emoji = "📊",
                    color = Color(0xFFF9FBE7),
                    onClick = { navigator.push(ReportesScreen(admin)) }
                )

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { navigator.replaceAll(LoginScreen()) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cerrar sesión", color = ITSURGrisMedio)
                }
            }
        }
    }
}

@Composable
fun AdminMenuCard(
    titulo: String,
    descripcion: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ITSURTexto)
                Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = ITSURGrisMedio)
            }
            Text("→", fontSize = 20.sp, color = ITSURVerde, fontWeight = FontWeight.Bold)
        }
    }
}
