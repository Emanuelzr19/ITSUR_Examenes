package mx.itsur.exams.domain.usecase

import mx.itsur.exams.data.local.hashPassword
import mx.itsur.exams.data.local.verifyPassword
import mx.itsur.exams.domain.model.*
import mx.itsur.exams.domain.repository.AlumnoRepository
import mx.itsur.exams.domain.repository.ExamenRepository
import mx.itsur.exams.domain.repository.ResultadoRepository

class LoginUseCase(private val alumnoRepository: AlumnoRepository) {
    suspend operator fun invoke(email: String, password: String): Alumno? {
        val alumno = alumnoRepository.getAlumnoByEmail(email) ?: return null
        return if (verifyPassword(password, alumno.passwordHash)) alumno else null
    }
}

class GetExamenesUseCase(private val examenRepository: ExamenRepository) {
    suspend operator fun invoke(soloActivos: Boolean = false): List<Examen> =
        if (soloActivos) examenRepository.getExamenesActivos()
        else examenRepository.getAllExamenes()
}

class GetExamenByIdUseCase(private val examenRepository: ExamenRepository) {
    suspend operator fun invoke(id: String): Examen? = examenRepository.getExamenById(id)
}

class CrearExamenUseCase(private val examenRepository: ExamenRepository) {
    suspend operator fun invoke(examen: Examen) = examenRepository.insertExamen(examen)
}

class ActualizarExamenUseCase(private val examenRepository: ExamenRepository) {
    suspend operator fun invoke(examen: Examen) = examenRepository.updateExamen(examen)
}

class EliminarExamenUseCase(private val examenRepository: ExamenRepository) {
    suspend operator fun invoke(id: String) = examenRepository.deleteExamen(id)
}

/**
 * Califica un examen comparando las respuestas del alumno con las opciones correctas
 * y calcula la calificación en escala de 0 a 10.
 */
class CalificarExamenUseCase {
    operator fun invoke(examen: Examen, respuestasMap: Map<String, String?>): Pair<Float, List<RespuestaAlumno>> {
        val resultadoId = generarId()
        var puntajeObtenido = 0f
        val puntajeTotal = examen.preguntas.sumOf { it.puntaje.toDouble() }.toFloat()

        val respuestas = examen.preguntas.map { pregunta ->
            val opcionSeleccionadaId = respuestasMap[pregunta.id]
            val esCorrecta = when (pregunta.tipo) {
                TipoPregunta.OPCION_MULTIPLE, TipoPregunta.VERDADERO_FALSO -> {
                    val correcta = pregunta.opciones.find { it.esCorrecta }
                    opcionSeleccionadaId != null && opcionSeleccionadaId == correcta?.id
                }
                TipoPregunta.ABIERTA -> false
            }
            if (esCorrecta) puntajeObtenido += pregunta.puntaje

            RespuestaAlumno(
                id = generarId(),
                resultadoId = resultadoId,
                preguntaId = pregunta.id,
                opcionSeleccionadaId = opcionSeleccionadaId,
                respuestaTexto = null,
                esCorrecta = esCorrecta
            )
        }

        val calificacion = if (puntajeTotal > 0) (puntajeObtenido / puntajeTotal) * 10f else 0f
        return Pair(calificacion, respuestas)
    }
}

class GetAlumnosUseCase(private val alumnoRepository: AlumnoRepository) {
    suspend operator fun invoke(): List<Alumno> = alumnoRepository.getAllAlumnos()
}

class RegistrarAlumnoUseCase(private val alumnoRepository: AlumnoRepository) {
    suspend operator fun invoke(alumno: Alumno, password: String) {
        val alumnoConHash = alumno.copy(passwordHash = hashPassword(password))
        alumnoRepository.insertAlumno(alumnoConHash)
    }
}

class EliminarAlumnoUseCase(private val alumnoRepository: AlumnoRepository) {
    suspend operator fun invoke(id: String) = alumnoRepository.deleteAlumno(id)
}

class GetResultadosByAlumnoUseCase(private val resultadoRepository: ResultadoRepository) {
    suspend operator fun invoke(alumnoId: String): List<Resultado> =
        resultadoRepository.getResultadosByAlumno(alumnoId)
}

class GetResultadosByExamenUseCase(private val resultadoRepository: ResultadoRepository) {
    suspend operator fun invoke(examenId: String): List<Resultado> =
        resultadoRepository.getResultadosByExamen(examenId)
}

class GuardarResultadoUseCase(private val resultadoRepository: ResultadoRepository) {
    suspend operator fun invoke(resultado: Resultado, respuestas: List<RespuestaAlumno>) =
        resultadoRepository.insertResultado(resultado, respuestas)
}

class AlumnoYaTomoPruebaUseCase(private val resultadoRepository: ResultadoRepository) {
    suspend operator fun invoke(alumnoId: String, examenId: String): Boolean =
        resultadoRepository.alumnoYaTomoPrueba(alumnoId, examenId)
}

fun generarId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..16).map { chars.random() }.joinToString("")
}
