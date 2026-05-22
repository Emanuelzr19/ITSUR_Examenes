package mx.itsur.exams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.domain.model.TipoPregunta
import mx.itsur.exams.presentation.viewmodel.*
import mx.itsur.exams.ui.components.*
import mx.itsur.exams.ui.theme.*
import mx.itsur.exams.util.format1f

data class DashboardAlumnoScreen(val alumno: Alumno) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ExamenListViewModel>()
        val examenes by viewModel.examenes.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        LaunchedEffect(Unit) { viewModel.loadExamenes(soloActivos = true) }

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
                        Text("Hola, ${alumno.nombre.split(" ").first()} 👋", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(alumno.grupo, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                    ITSURLogo(56)
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mis Exámenes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { navigator.push(MisResultadosScreen(alumno)) }) {
                        Text("Ver resultados →", color = ITSURVerde, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isLoading) {
                    LoadingIndicator()
                } else if (examenes.isEmpty()) {
                    EmptyState(mensaje = "No hay exámenes disponibles.", emoji = "📚")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(examenes, key = { it.id }) { examen ->
                            ExamenCard(
                                titulo = examen.titulo,
                                descripcion = examen.descripcion,
                                numPreguntas = examen.preguntas.size,
                                duracionMinutos = examen.duracionMinutos,
                                activo = examen.activo,
                                onClick = { navigator.push(AplicarExamenScreen(alumno, examen.id)) }
                            )
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { navigator.replaceAll(LoginScreen()) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Cerrar sesión", color = ITSURGrisMedio) }
                        }
                    }
                }
            }
        }
    }
}

data class AplicarExamenScreen(val alumno: Alumno, val examenId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<AplicarExamenViewModel>()
        val examen by viewModel.examen.collectAsState()
        val respuestas by viewModel.respuestas.collectAsState()
        val preguntaActual by viewModel.preguntaActual.collectAsState()
        val resultado by viewModel.resultado.collectAsState()
        val yaTomoPrueba by viewModel.yaTomoPrueba.collectAsState()

        LaunchedEffect(Unit) { viewModel.loadExamen(examenId, alumno.id) }

        resultado?.let { r ->
            ResultadoFinalScreen(resultado = r, alumno = alumno, onVolver = { navigator.popUntilRoot() })
            return
        }

        if (yaTomoPrueba) {
            Column(
                modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⚠️", fontSize = 56.sp)
                Spacer(Modifier.height(16.dp))
                Text("Ya realizaste este examen", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("No puedes repetirlo.", color = ITSURGrisMedio, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                ITSURButton(text = "Regresar", onClick = { navigator.pop() })
            }
            return
        }

        examen?.let { e ->
            val preguntas = e.preguntas
            if (preguntas.isEmpty()) return@let
            val pregunta = preguntas[preguntaActual]

            Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
                ITSURHeader(
                    title = e.titulo,
                    subtitle = "${preguntaActual + 1} de ${preguntas.size}",
                    onBackClick = { navigator.pop() }
                )

                LinearProgressIndicator(
                    progress = { (preguntaActual + 1).toFloat() / preguntas.size },
                    modifier = Modifier.fillMaxWidth(),
                    color = ITSURVerde,
                    trackColor = ITSURGrisClaro
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(ITSURVerdeClaro),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                ChipInfo(label = "Pregunta ${preguntaActual + 1} • ${pregunta.puntaje} pts")
                                Spacer(Modifier.height(10.dp))
                                Text(pregunta.texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ITSURTexto)
                            }
                        }
                    }

                    if (pregunta.tipo != TipoPregunta.ABIERTA) {
                        items(pregunta.opciones) { opcion ->
                            val isSelected = respuestas[pregunta.id] == opcion.id
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    if (isSelected) ITSURVerde else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
                                onClick = { viewModel.seleccionarRespuesta(pregunta.id, opcion.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.seleccionarRespuesta(pregunta.id, opcion.id) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color.White,
                                            unselectedColor = ITSURGrisMedio
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        opcion.texto,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) Color.White else ITSURTexto,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (preguntaActual > 0) {
                        ITSURButton(
                            text = "← Anterior",
                            onClick = { viewModel.preguntaAnterior() },
                            modifier = Modifier.weight(1f),
                            secondary = true
                        )
                    }
                    if (preguntaActual < preguntas.size - 1) {
                        ITSURButton(
                            text = "Siguiente →",
                            onClick = { viewModel.siguientePregunta() },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        ITSURButton(
                            text = "Finalizar examen ✓",
                            onClick = { viewModel.finalizarExamen(alumno) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } ?: LoadingIndicator()
    }
}

@Composable
fun ResultadoFinalScreen(resultado: mx.itsur.exams.domain.model.Resultado, alumno: Alumno, onVolver: () -> Unit) {
    val aprobado = resultado.calificacion >= 6f
    Column(
        modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(if (aprobado) ITSURVerdeOscuro else ITSURError, if (aprobado) ITSURVerde else ITSURError.copy(alpha = 0.7f))))
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (aprobado) "🎉" else "📚", fontSize = 52.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = resultado.calificacion.format1f(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = if (aprobado) "¡Aprobado!" else "No aprobado",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(resultado.examenTitulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(6.dp))
                        Text(alumno.nombre, color = ITSURGrisMedio)
                        val mins = resultado.tiempoUsadoSegundos / 60
                        val secs = resultado.tiempoUsadoSegundos % 60
                        Text("Tiempo: ${mins}m ${secs}s", color = ITSURGrisMedio, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                QRResultado(resultado = resultado)
            }
            item {
                Spacer(Modifier.height(8.dp))
                ITSURButton(text = "Volver al inicio", onClick = onVolver, modifier = Modifier.fillMaxWidth().height(52.dp))
            }
        }
    }
}

data class MisResultadosScreen(val alumno: Alumno) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ReportesViewModel>()
        val resultados by viewModel.resultados.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        LaunchedEffect(Unit) { viewModel.loadResultadosByAlumno(alumno.id) }

        Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
            ITSURHeader(title = "Mis Resultados", subtitle = alumno.nombre, onBackClick = { navigator.pop() })

            if (isLoading) {
                LoadingIndicator()
            } else if (resultados.isEmpty()) {
                EmptyState(mensaje = "Aún no has realizado ningún examen.", emoji = "📋")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(resultados, key = { it.id }) { r ->
                        ResultadoCard(resultado = r)
                    }
                }
            }
        }
    }
}
