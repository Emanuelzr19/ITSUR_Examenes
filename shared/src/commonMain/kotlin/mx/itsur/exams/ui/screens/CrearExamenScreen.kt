package mx.itsur.exams.ui.screens
import mx.itsur.exams.util.currentTimeMs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import mx.itsur.exams.domain.model.*
import mx.itsur.exams.domain.usecase.generarId
import mx.itsur.exams.presentation.viewmodel.CrearExamenViewModel
import mx.itsur.exams.presentation.viewmodel.FormState
import mx.itsur.exams.ui.components.*
import mx.itsur.exams.ui.theme.*

data class CrearExamenScreen(val admin: Alumno, val examenId: String? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<CrearExamenViewModel>()
        val state by viewModel.state.collectAsState()
        val examenEditando by viewModel.examenEditando.collectAsState()

        var titulo by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var duracion by remember { mutableStateOf("60") }
        var activo by remember { mutableStateOf(true) }
        var preguntas by remember { mutableStateOf<List<PreguntaForm>>(emptyList()) }
        var errorMsg by remember { mutableStateOf("") }

        LaunchedEffect(examenId) {
            if (examenId != null) viewModel.loadExamen(examenId)
        }

        LaunchedEffect(examenEditando) {
            examenEditando?.let { e ->
                titulo = e.titulo
                descripcion = e.descripcion
                duracion = e.duracionMinutos.toString()
                activo = e.activo
                preguntas = e.preguntas.map { p ->
                    PreguntaForm(
                        id = p.id,
                        texto = p.texto,
                        tipo = p.tipo,
                        puntaje = p.puntaje.toString(),
                        opciones = p.opciones.map { o ->
                            OpcionForm(id = o.id, texto = o.texto, esCorrecta = o.esCorrecta)
                        }.toMutableList()
                    )
                }
            }
        }

        LaunchedEffect(state) {
            if (state is FormState.Success) {
                viewModel.resetState()
                navigator.pop()
            }
        }

        Column(modifier = Modifier.fillMaxSize().background(ITSURBlancoRoto)) {
            ITSURHeader(
                title = if (examenId == null) "Nuevo Examen" else "Editar Examen",
                onBackClick = { navigator.pop() }
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Información General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            ITSURTextField(value = titulo, onValueChange = { titulo = it }, label = "Título del examen")
                            ITSURTextField(value = descripcion, onValueChange = { descripcion = it }, label = "Descripción", singleLine = false, maxLines = 3)
                            ITSURTextField(value = duracion, onValueChange = { duracion = it }, label = "Duración (minutos)")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = activo, onCheckedChange = { activo = it }, colors = SwitchDefaults.colors(checkedThumbColor = ITSURVerde, checkedTrackColor = ITSURVerdeClaro))
                                Spacer(Modifier.width(8.dp))
                                Text(if (activo) "Examen activo" else "Examen inactivo", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preguntas (${preguntas.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        ITSURButton(
                            text = "Agregar",
                            icon = Icons.Default.Add,
                            onClick = {
                                preguntas = preguntas + PreguntaForm(
                                    id = generarId(),
                                    texto = "",
                                    tipo = TipoPregunta.OPCION_MULTIPLE,
                                    puntaje = "1.0",
                                    opciones = mutableListOf(
                                        OpcionForm(generarId(), "", false),
                                        OpcionForm(generarId(), "", false),
                                        OpcionForm(generarId(), "", true),
                                        OpcionForm(generarId(), "", false)
                                    )
                                )
                            }
                        )
                    }
                }

                itemsIndexed(preguntas) { idx, preguntaForm ->
                    PreguntaEditor(
                        numero = idx + 1,
                        form = preguntaForm,
                        onUpdate = { updated ->
                            preguntas = preguntas.toMutableList().also { it[idx] = updated }
                        },
                        onDelete = {
                            preguntas = preguntas.toMutableList().also { it.removeAt(idx) }
                        }
                    )
                }

                item {
                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = ITSURError, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    ITSURButton(
                        text = if (state is FormState.Loading) "Guardando..." else "Guardar Examen",
                        icon = Icons.Default.Save,
                        onClick = {
                            if (titulo.isBlank()) { errorMsg = "El título es requerido"; return@ITSURButton }
                            if (preguntas.isEmpty()) { errorMsg = "Agrega al menos una pregunta"; return@ITSURButton }
                            val examenId2 = examenId ?: generarId()
                            val examen = Examen(
                                id = examenId2,
                                titulo = titulo,
                                descripcion = descripcion,
                                duracionMinutos = duracion.toIntOrNull() ?: 60,
                                fechaCreacion = currentTimeMs(),
                                creadoPor = admin.id,
                                activo = activo,
                                preguntas = preguntas.mapIndexed { i, pf ->
                                    Pregunta(
                                        id = pf.id,
                                        examenId = examenId2,
                                        texto = pf.texto,
                                        tipo = pf.tipo,
                                        puntaje = pf.puntaje.toFloatOrNull() ?: 1f,
                                        orden = i,
                                        opciones = pf.opciones.map { of ->
                                            Opcion(id = of.id, preguntaId = pf.id, texto = of.texto, esCorrecta = of.esCorrecta)
                                        }
                                    )
                                }
                            )
                            viewModel.guardar(examen)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = state !is FormState.Loading
                    )
                }
            }
        }
    }
}

@Composable
fun PreguntaEditor(numero: Int, form: PreguntaForm, onUpdate: (PreguntaForm) -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pregunta $numero", fontWeight = FontWeight.Bold, color = ITSURVerde)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar pregunta", tint = ITSURError)
                }
            }

            ITSURTextField(
                value = form.texto,
                onValueChange = { onUpdate(form.copy(texto = it)) },
                label = "Texto de la pregunta",
                singleLine = false,
                maxLines = 3
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoPregunta.values().forEach { tipo ->
                    FilterChip(
                        selected = form.tipo == tipo,
                        onClick = {
                            // When switching to VERDADERO_FALSO, set exactly 2 options
                            val nuevasOpciones = when (tipo) {
                                TipoPregunta.VERDADERO_FALSO -> mutableListOf(
                                    OpcionForm(generarId(), "Verdadero", true),
                                    OpcionForm(generarId(), "Falso", false)
                                )
                                TipoPregunta.OPCION_MULTIPLE -> if (form.tipo == TipoPregunta.VERDADERO_FALSO || form.tipo == TipoPregunta.ABIERTA) mutableListOf(
                                    OpcionForm(generarId(), "", false),
                                    OpcionForm(generarId(), "", false),
                                    OpcionForm(generarId(), "", true),
                                    OpcionForm(generarId(), "", false)
                                ) else form.opciones
                                TipoPregunta.ABIERTA -> mutableListOf()
                            }
                            onUpdate(form.copy(tipo = tipo, opciones = nuevasOpciones))
                        },
                        label = {
                            Text(
                                when (tipo) {
                                    TipoPregunta.OPCION_MULTIPLE -> "Opción múltiple"
                                    TipoPregunta.VERDADERO_FALSO -> "Verdadero/Falso"
                                    TipoPregunta.ABIERTA -> "Abierta"
                                },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ITSURVerde,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            ITSURTextField(
                value = form.puntaje,
                onValueChange = { onUpdate(form.copy(puntaje = it)) },
                label = "Puntaje"
            )

            when (form.tipo) {
                TipoPregunta.ABIERTA -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ITSURGrisMedio, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "El alumno escribirá su respuesta libremente. Requiere revisión manual.",
                            style = MaterialTheme.typography.labelSmall,
                            color = ITSURGrisMedio
                        )
                    }
                }
                TipoPregunta.VERDADERO_FALSO -> {
                    Text("Marca la respuesta correcta:", style = MaterialTheme.typography.labelLarge, color = ITSURGrisMedio)
                    form.opciones.forEachIndexed { i, opcion ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = opcion.esCorrecta,
                                onClick = {
                                    val nuevasOpciones = form.opciones.mapIndexed { j, o -> o.copy(esCorrecta = j == i) }.toMutableList()
                                    onUpdate(form.copy(opciones = nuevasOpciones))
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ITSURVerde)
                            )
                            Text(
                                opcion.texto,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (opcion.esCorrecta) FontWeight.Bold else FontWeight.Normal,
                                color = if (opcion.esCorrecta) ITSURVerdeOscuro else ITSURTexto
                            )
                        }
                    }
                }
                TipoPregunta.OPCION_MULTIPLE -> {
                    Text("Opciones (marca la correcta):", style = MaterialTheme.typography.labelLarge, color = ITSURGrisMedio)
                    form.opciones.forEachIndexed { i, opcion ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = opcion.esCorrecta,
                                onClick = {
                                    val nuevasOpciones = form.opciones.mapIndexed { j, o -> o.copy(esCorrecta = j == i) }.toMutableList()
                                    onUpdate(form.copy(opciones = nuevasOpciones))
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ITSURVerde)
                            )
                            OutlinedTextField(
                                value = opcion.texto,
                                onValueChange = { nuevoTexto ->
                                    val nuevasOpciones = form.opciones.toMutableList().also { it[i] = opcion.copy(texto = nuevoTexto) }
                                    onUpdate(form.copy(opciones = nuevasOpciones))
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Opción ${i + 1}", color = ITSURGrisMedio) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ITSURVerde)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class PreguntaForm(
    val id: String,
    val texto: String,
    val tipo: TipoPregunta,
    val puntaje: String,
    val opciones: MutableList<OpcionForm>
)

data class OpcionForm(val id: String, val texto: String, val esCorrecta: Boolean)
