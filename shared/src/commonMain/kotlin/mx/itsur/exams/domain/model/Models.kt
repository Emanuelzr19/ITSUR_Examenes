package mx.itsur.exams.domain.model

data class Alumno(
    val id: String,
    val nombre: String,
    val email: String,
    val numeroControl: String,
    val grupo: String,
    val rol: Rol,
    val passwordHash: String,
    val fechaRegistro: Long
)

data class Examen(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val duracionMinutos: Int,
    val fechaCreacion: Long,
    val creadoPor: String,
    val activo: Boolean,
    val preguntas: List<Pregunta> = emptyList()
)

data class Pregunta(
    val id: String,
    val examenId: String,
    val texto: String,
    val tipo: TipoPregunta,
    val puntaje: Float,
    val orden: Int,
    val opciones: List<Opcion> = emptyList()
)

data class Opcion(
    val id: String,
    val preguntaId: String,
    val texto: String,
    val esCorrecta: Boolean
)

data class Resultado(
    val id: String,
    val alumnoId: String,
    val examenId: String,
    val calificacion: Float,
    val tiempoUsadoSegundos: Int,
    val fechaAplicacion: Long,
    val examenTitulo: String = "",
    val alumnoNombre: String = "",
    val numeroControl: String = ""
)

data class RespuestaAlumno(
    val id: String,
    val resultadoId: String,
    val preguntaId: String,
    val opcionSeleccionadaId: String?,
    val respuestaTexto: String?,
    val esCorrecta: Boolean
)

enum class TipoPregunta {
    OPCION_MULTIPLE,
    VERDADERO_FALSO,
    ABIERTA
}

enum class Rol {
    ADMIN,
    ALUMNO
}
