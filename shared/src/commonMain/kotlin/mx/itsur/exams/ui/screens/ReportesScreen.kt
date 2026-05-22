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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel
import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.presentation.viewmodel.ReportesViewModel
import mx.itsur.exams.ui.components.*
import mx.itsur.exams.ui.theme.*
import mx.itsur.exams.util.format1f

data class ReportesScreen(val admin: Alumno) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ReportesViewModel>()
        val examenes by viewModel.examenes.collectAsState()
        val resultados by viewModel.resultados.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        var examenSeleccionado by remember { mutableStateOf<String?>(null) }
        var examenNombre by remember { mutableStateOf("") }

        LaunchedEffect(Unit) { viewModel.loadExamenes() }

        LaunchedEffect(examenSeleccionado) {
            examenSeleccionado?.let { viewModel.loadResultadosByExamen(it) }
        }

        Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
            ITSURHeader(title = "Reportes", subtitle = "Calificaciones por examen", onBackClick = { navigator.pop() })

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Seleccionar examen:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    examenes.forEach { examen ->
                        val isSelected = examenSeleccionado == examen.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                examenSeleccionado = examen.id
                                examenNombre = examen.titulo
                            },
                            label = { Text(examen.titulo) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ITSURVerde,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (examenSeleccionado != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val promedio = if (resultados.isNotEmpty())
                                resultados.sumOf { it.calificacion.toDouble() } / resultados.size
                            else 0.0
                            val aprobados = resultados.count { it.calificacion >= 6f }

                            StatCard(
                                titulo = "Presentaron",
                                valor = "${resultados.size}",
                                emoji = "👥",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                titulo = "Promedio",
                                valor = promedio.format1f(),
                                emoji = "📊",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                titulo = "Aprobados",
                                valor = "$aprobados",
                                emoji = "✅",
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text(
                            "Resultados — $examenNombre",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isLoading) {
                        item { LoadingIndicator() }
                    } else if (resultados.isEmpty()) {
                        item { EmptyState(mensaje = "Nadie ha realizado este examen aún.", emoji = "📋") }
                    } else {
                        items(resultados, key = { it.id }) { r ->
                            ResultadoCard(resultado = r)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(titulo: String, valor: String, emoji: String, color: Color = ITSURVerdeClaro, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ITSURVerdeOscuro)
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = ITSURGrisMedio)
        }
    }
}
