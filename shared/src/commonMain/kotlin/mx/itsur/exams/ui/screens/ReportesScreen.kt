package mx.itsur.exams.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel
import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.domain.model.Resultado
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
        var exportMsg by remember { mutableStateOf("") }

        LaunchedEffect(Unit) { viewModel.loadExamenes() }
        LaunchedEffect(examenSeleccionado) {
            examenSeleccionado?.let { viewModel.loadResultadosByExamen(it) }
        }

        Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
            ITSURHeader(
                title = "Reportes",
                subtitle = "Calificaciones por examen",
                onBackClick = { navigator.pop() }
            )

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
                                exportMsg = ""
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
                                resultados.sumOf { it.calificacion.toDouble() } / resultados.size else 0.0
                            val aprobados = resultados.count { it.calificacion >= 6f }

                            ReporteStatCard("Presentaron", "${resultados.size}", Icons.Default.Group, modifier = Modifier.weight(1f))
                            ReporteStatCard("Promedio", promedio.format1f(), Icons.Default.BarChart, modifier = Modifier.weight(1f))
                            ReporteStatCard("Aprobados", "$aprobados", Icons.Default.CheckCircle, color = Color(0xFFE8F5E9), modifier = Modifier.weight(1f))
                        }
                    }

                    if (resultados.isNotEmpty()) {
                        item {
                            GraficaCalificaciones(resultados = resultados)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ITSURButton(
                                    text = "Exportar PDF",
                                    icon = Icons.Default.PictureAsPdf,
                                    onClick = { exportMsg = exportarPDF(examenNombre, resultados) },
                                    modifier = Modifier.weight(1f),
                                    secondary = true
                                )
                                ITSURButton(
                                    text = "Exportar Excel",
                                    icon = Icons.Default.TableChart,
                                    onClick = { exportMsg = exportarExcel(examenNombre, resultados) },
                                    modifier = Modifier.weight(1f),
                                    secondary = true
                                )
                            }
                            if (exportMsg.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(ITSURVerdeClaro)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ITSURVerdeOscuro, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(exportMsg, style = MaterialTheme.typography.bodySmall, color = ITSURVerdeOscuro)
                                    }
                                }
                            }
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
                        item { EmptyState(mensaje = "Nadie ha realizado este examen aún.", icon = Icons.Default.Assignment) }
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
fun GraficaCalificaciones(resultados: List<Resultado>) {
    data class Rebanada(val label: String, val count: Int, val color: Color)

    val rebanadas = listOf(
        Rebanada("0-5",  resultados.count { it.calificacion < 6f },                                  Color(0xFFE53935)),
        Rebanada("6",    resultados.count { it.calificacion >= 6f && it.calificacion < 7f },          Color(0xFFFFB300)),
        Rebanada("7",    resultados.count { it.calificacion >= 7f && it.calificacion < 8f },          Color(0xFF66BB6A)),
        Rebanada("8",    resultados.count { it.calificacion >= 8f && it.calificacion < 9f },          Color(0xFF43A047)),
        Rebanada("9",    resultados.count { it.calificacion >= 9f && it.calificacion < 10f },         Color(0xFF2E7D32)),
        Rebanada("10",   resultados.count { it.calificacion >= 10f },                                 Color(0xFF1B5E20)),
    ).filter { it.count > 0 }

    val total = rebanadas.sumOf { it.count }.toFloat().coerceAtLeast(1f)
    val aprobados = resultados.count { it.calificacion >= 6f }
    val reprobados = resultados.size - aprobados

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, contentDescription = null, tint = ITSURVerde, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Distribución de calificaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Pie chart canvas
                Canvas(modifier = Modifier.size(180.dp)) {
                    val diameter = min(size.width, size.height)
                    val radius = diameter / 2f
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)
                    val holeRadius = radius * 0.52f  // donut hole
                    val strokeGap = 2f

                    var startAngle = -90f
                    rebanadas.forEach { rebanada ->
                        val sweep = (rebanada.count / total) * 360f
                        // outer arc fill
                        drawArc(
                            color = rebanada.color,
                            startAngle = startAngle + strokeGap / 2,
                            sweepAngle = sweep - strokeGap,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize
                        )
                        startAngle += sweep
                    }
                    // donut hole (white circle in center)
                    drawCircle(
                        color = Color.White,
                        radius = holeRadius,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                    // thin border on hole
                    drawCircle(
                        color = Color(0xFFEEEEEE),
                        radius = holeRadius,
                        center = Offset(size.width / 2f, size.height / 2f),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Legend column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Summary at top
                    Text(
                        "${resultados.size} alumnos",
                        style = MaterialTheme.typography.labelMedium,
                        color = ITSURGrisMedio,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    rebanadas.forEach { rebanada ->
                        val pct = ((rebanada.count / total) * 100).toInt()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(rebanada.color, RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = "${rebanada.label}  —  ${rebanada.count} ($pct%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ITSURTexto
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = ITSURGrisClaro)
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "✓ Aprobados: $aprobados",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "✗ Reprobados: $reprobados",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

expect fun exportarPDF(examenNombre: String, resultados: List<Resultado>): String
expect fun exportarExcel(examenNombre: String, resultados: List<Resultado>): String

@Composable
fun ReporteStatCard(
    titulo: String,
    valor: String,
    icon: ImageVector,
    color: Color = ITSURVerdeClaro,
    modifier: Modifier = Modifier
) {
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
            Icon(icon, contentDescription = null, tint = ITSURVerdeOscuro, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(4.dp))
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ITSURVerdeOscuro)
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = ITSURGrisMedio)
        }
    }
}
