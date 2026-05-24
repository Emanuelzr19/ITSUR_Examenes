package mx.itsur.exams.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.koin.getScreenModel
import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.presentation.viewmodel.ExamenListViewModel
import mx.itsur.exams.ui.components.*
import mx.itsur.exams.ui.theme.*

data class ExamenListAdminScreen(val admin: Alumno) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ExamenListViewModel>()
        val examenes by viewModel.examenes.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        var showDeleteDialog by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) { viewModel.loadExamenes() }

        Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
            ITSURHeader(
                title = "Exámenes",
                subtitle = "${examenes.size} registrados",
                onBackClick = { navigator.pop() },
                actions = {
                    IconButton(onClick = { navigator.push(CrearExamenScreen(admin)) }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo examen", tint = Color.White)
                    }
                }
            )

            if (isLoading) {
                LoadingIndicator()
            } else if (examenes.isEmpty()) {
                EmptyState(mensaje = "No hay exámenes.\nPresiona + para crear uno.", icon = Icons.Default.Assignment)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(examenes, key = { it.id }) { examen ->
                        ExamenCard(
                            titulo = examen.titulo,
                            descripcion = examen.descripcion,
                            numPreguntas = examen.preguntas.size,
                            duracionMinutos = examen.duracionMinutos,
                            activo = examen.activo,
                            onClick = { navigator.push(CrearExamenScreen(admin, examen.id)) },
                            onDelete = { showDeleteDialog = examen.id }
                        )
                    }
                }
            }
        }

        showDeleteDialog?.let { id ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("¿Eliminar examen?") },
                text = { Text("Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminar(id)
                        showDeleteDialog = null
                    }) { Text("Eliminar", color = ITSURError) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
                }
            )
        }
    }
}
