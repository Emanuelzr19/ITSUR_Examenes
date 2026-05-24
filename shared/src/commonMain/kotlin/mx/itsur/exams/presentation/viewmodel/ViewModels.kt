package mx.itsur.exams.presentation.viewmodel
import mx.itsur.exams.util.currentTimeMs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itsur.exams.domain.model.*
import mx.itsur.exams.domain.usecase.*

class LoginViewModel(private val loginUseCase: LoginUseCase) : ScreenModel {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        screenModelScope.launch {
            _state.value = LoginState.Loading
            val alumno = loginUseCase(email, password)
            _state.value = if (alumno != null) LoginState.Success(alumno) else LoginState.Error("Credenciales incorrectas")
        }
    }

    fun resetState() { _state.value = LoginState.Idle }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val alumno: Alumno) : LoginState()
    data class Error(val message: String) : LoginState()
}

class ExamenListViewModel(
    private val getExamenesUseCase: GetExamenesUseCase,
    private val eliminarExamenUseCase: EliminarExamenUseCase
) : ScreenModel {
    private val _examenes = MutableStateFlow<List<Examen>>(emptyList())
    val examenes: StateFlow<List<Examen>> = _examenes.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadExamenes(soloActivos: Boolean = false) {
        screenModelScope.launch {
            _isLoading.value = true
            _examenes.value = getExamenesUseCase(soloActivos)
            _isLoading.value = false
        }
    }

    fun eliminar(id: String) {
        screenModelScope.launch {
            eliminarExamenUseCase(id)
            loadExamenes()
        }
    }
}

class CrearExamenViewModel(
    private val crearExamenUseCase: CrearExamenUseCase,
    private val actualizarExamenUseCase: ActualizarExamenUseCase,
    private val getExamenByIdUseCase: GetExamenByIdUseCase
) : ScreenModel {
    private val _state = MutableStateFlow<FormState>(FormState.Idle)
    val state: StateFlow<FormState> = _state.asStateFlow()

    private val _examenEditando = MutableStateFlow<Examen?>(null)
    val examenEditando: StateFlow<Examen?> = _examenEditando.asStateFlow()

    fun loadExamen(id: String) {
        screenModelScope.launch {
            _examenEditando.value = getExamenByIdUseCase(id)
        }
    }

    fun guardar(examen: Examen) {
        screenModelScope.launch {
            _state.value = FormState.Loading
            try {
                if (_examenEditando.value != null) actualizarExamenUseCase(examen)
                else crearExamenUseCase(examen)
                _state.value = FormState.Success
            } catch (e: Exception) {
                _state.value = FormState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() { _state.value = FormState.Idle }
}

sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    object Success : FormState()
    data class Error(val message: String) : FormState()
}

class AplicarExamenViewModel(
    private val getExamenByIdUseCase: GetExamenByIdUseCase,
    private val calificarUseCase: CalificarExamenUseCase,
    private val guardarResultadoUseCase: GuardarResultadoUseCase,
    private val alumnoYaTomoPruebaUseCase: AlumnoYaTomoPruebaUseCase
) : ScreenModel {
    private val _examen = MutableStateFlow<Examen?>(null)
    val examen: StateFlow<Examen?> = _examen.asStateFlow()

    private val _respuestas = MutableStateFlow<Map<String, String?>>(emptyMap())
    val respuestas: StateFlow<Map<String, String?>> = _respuestas.asStateFlow()

    // Map for open-text answers: preguntaId -> texto
    private val _respuestasTexto = MutableStateFlow<Map<String, String>>(emptyMap())
    val respuestasTexto: StateFlow<Map<String, String>> = _respuestasTexto.asStateFlow()

    private val _preguntaActual = MutableStateFlow(0)
    val preguntaActual: StateFlow<Int> = _preguntaActual.asStateFlow()

    private val _resultado = MutableStateFlow<Resultado?>(null)
    val resultado: StateFlow<Resultado?> = _resultado.asStateFlow()

    private val _yaTomoPrueba = MutableStateFlow(false)
    val yaTomoPrueba: StateFlow<Boolean> = _yaTomoPrueba.asStateFlow()

    private var tiempoInicioSegundos: Long = 0

    fun loadExamen(examenId: String, alumnoId: String) {
        screenModelScope.launch {
            _yaTomoPrueba.value = alumnoYaTomoPruebaUseCase(alumnoId, examenId)
            _examen.value = getExamenByIdUseCase(examenId)
            tiempoInicioSegundos = currentTimeMs() / 1000
        }
    }

    fun seleccionarRespuesta(preguntaId: String, opcionId: String?) {
        _respuestas.value = _respuestas.value.toMutableMap().apply { put(preguntaId, opcionId) }
    }

    fun escribirRespuestaAbierta(preguntaId: String, texto: String) {
        _respuestasTexto.value = _respuestasTexto.value.toMutableMap().apply { put(preguntaId, texto) }
    }

    fun siguientePregunta() {
        val total = _examen.value?.preguntas?.size ?: 0
        if (_preguntaActual.value < total - 1) _preguntaActual.value++
    }

    fun preguntaAnterior() {
        if (_preguntaActual.value > 0) _preguntaActual.value--
    }

    fun finalizarExamen(alumno: Alumno) {
        screenModelScope.launch {
            val examen = _examen.value ?: return@launch
            val tiempoUsado = (currentTimeMs() / 1000 - tiempoInicioSegundos).toInt()
            val (calificacion, respuestas) = calificarUseCase(examen, _respuestas.value, _respuestasTexto.value)
            val resultadoId = generarId()
            val resultado = Resultado(
                id = resultadoId,
                alumnoId = alumno.id,
                examenId = examen.id,
                calificacion = calificacion,
                tiempoUsadoSegundos = tiempoUsado,
                fechaAplicacion = currentTimeMs(),
                examenTitulo = examen.titulo
            )
            val respuestasConId = respuestas.map { it.copy(resultadoId = resultadoId) }
            guardarResultadoUseCase(resultado, respuestasConId)
            _resultado.value = resultado
        }
    }
}

class AlumnoListViewModel(
    private val getAlumnosUseCase: GetAlumnosUseCase,
    private val registrarAlumnoUseCase: RegistrarAlumnoUseCase,
    private val eliminarAlumnoUseCase: EliminarAlumnoUseCase,
    private val actualizarAlumnoUseCase: ActualizarAlumnoUseCase
) : ScreenModel {
    private val _alumnos = MutableStateFlow<List<Alumno>>(emptyList())
    val alumnos: StateFlow<List<Alumno>> = _alumnos.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadAlumnos() {
        screenModelScope.launch {
            _isLoading.value = true
            _alumnos.value = getAlumnosUseCase()
            _isLoading.value = false
        }
    }

    fun registrar(alumno: Alumno, password: String) {
        screenModelScope.launch {
            registrarAlumnoUseCase(alumno, password)
            loadAlumnos()
        }
    }

    fun actualizar(alumno: Alumno) {
        screenModelScope.launch {
            actualizarAlumnoUseCase(alumno)
            loadAlumnos()
        }
    }

    fun eliminar(id: String) {
        screenModelScope.launch {
            eliminarAlumnoUseCase(id)
            loadAlumnos()
        }
    }
}

class ReportesViewModel(
    private val getResultadosByExamenUseCase: GetResultadosByExamenUseCase,
    private val getResultadosByAlumnoUseCase: GetResultadosByAlumnoUseCase,
    private val getExamenesUseCase: GetExamenesUseCase
) : ScreenModel {
    private val _resultados = MutableStateFlow<List<Resultado>>(emptyList())
    val resultados: StateFlow<List<Resultado>> = _resultados.asStateFlow()
    private val _examenes = MutableStateFlow<List<Examen>>(emptyList())
    val examenes: StateFlow<List<Examen>> = _examenes.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadExamenes() {
        screenModelScope.launch {
            _examenes.value = getExamenesUseCase()
        }
    }

    fun loadResultadosByExamen(examenId: String) {
        screenModelScope.launch {
            _isLoading.value = true
            _resultados.value = getResultadosByExamenUseCase(examenId)
            _isLoading.value = false
        }
    }

    fun loadResultadosByAlumno(alumnoId: String) {
        screenModelScope.launch {
            _isLoading.value = true
            _resultados.value = getResultadosByAlumnoUseCase(alumnoId)
            _isLoading.value = false
        }
    }
}
