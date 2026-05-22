package mx.itsur.exams.data.local
import mx.itsur.exams.util.currentTimeMs

import mx.itsur.exams.db.ITSURDatabase
import mx.itsur.exams.domain.model.*
import mx.itsur.exams.util.currentTimeMs

fun createDatabase(driverFactory: DatabaseDriverFactory): ITSURDatabase {
    val driver = driverFactory.createDriver()
    val database = ITSURDatabase(driver)
    seedAdminIfNeeded(database)
    return database
}

/**
 * Inserta el usuario administrador por defecto si no existe ninguno,
 * garantizando que siempre haya acceso inicial al sistema.
 */
private fun seedAdminIfNeeded(database: ITSURDatabase) {
    val adminExists = database.iTSURDatabaseQueries.selectAlumnoByEmail("admin@itsur.edu.mx")
        .executeAsOneOrNull()
    if (adminExists == null) {
        database.iTSURDatabaseQueries.insertAlumno(
            id = "admin-001",
            nombre = "Administrador ITSUR",
            email = "admin@itsur.edu.mx",
            numero_control = "ADM001",
            grupo = "ADMIN",
            rol = "ADMIN",
            password_hash = hashPassword("admin123"),
            fecha_registro = currentTimeMs()
        )
    }
}

fun mapToAlumno(
    id: String, nombre: String, email: String, numero_control: String,
    grupo: String, rol: String, password_hash: String, fecha_registro: Long
) = Alumno(
    id = id, nombre = nombre, email = email, numeroControl = numero_control,
    grupo = grupo, rol = Rol.valueOf(rol), passwordHash = password_hash, fechaRegistro = fecha_registro
)

fun mapToExamen(
    id: String, titulo: String, descripcion: String, duracion_minutos: Long,
    fecha_creacion: Long, creado_por: String, activo: Long
) = Examen(
    id = id, titulo = titulo, descripcion = descripcion,
    duracionMinutos = duracion_minutos.toInt(), fechaCreacion = fecha_creacion,
    creadoPor = creado_por, activo = activo == 1L
)

fun mapToPregunta(
    id: String, examen_id: String, texto: String, tipo: String,
    puntaje: Double, orden: Long
) = Pregunta(
    id = id, examenId = examen_id, texto = texto,
    tipo = TipoPregunta.valueOf(tipo), puntaje = puntaje.toFloat(), orden = orden.toInt()
)

fun mapToOpcion(id: String, pregunta_id: String, texto: String, es_correcta: Long) =
    Opcion(id = id, preguntaId = pregunta_id, texto = texto, esCorrecta = es_correcta == 1L)

fun mapToResultado(
    id: String, alumno_id: String, examen_id: String, calificacion: Double,
    tiempo_usado_segundos: Long, fecha_aplicacion: Long,
    examenTitulo: String = "", alumnoNombre: String = "", numeroControl: String = ""
) = Resultado(
    id = id, alumnoId = alumno_id, examenId = examen_id,
    calificacion = calificacion.toFloat(), tiempoUsadoSegundos = tiempo_usado_segundos.toInt(),
    fechaAplicacion = fecha_aplicacion, examenTitulo = examenTitulo,
    alumnoNombre = alumnoNombre, numeroControl = numeroControl
)

fun mapToRespuesta(
    id: String, resultado_id: String, pregunta_id: String,
    opcion_seleccionada_id: String?, respuesta_texto: String?, es_correcta: Long
) = RespuestaAlumno(
    id = id, resultadoId = resultado_id, preguntaId = pregunta_id,
    opcionSeleccionadaId = opcion_seleccionada_id,
    respuestaTexto = respuesta_texto, esCorrecta = es_correcta == 1L
)

fun hashPassword(password: String): String {
    var hash = 0
    for (char in password) {
        hash = 31 * hash + char.code
    }
    return "hash_${hash.toString(16)}"
}

fun verifyPassword(password: String, hash: String): Boolean {
    return hashPassword(password) == hash
}
