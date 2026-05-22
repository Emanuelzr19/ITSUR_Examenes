package mx.itsur.exams.domain.repository

import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.domain.model.Examen
import mx.itsur.exams.domain.model.Pregunta
import mx.itsur.exams.domain.model.Resultado
import mx.itsur.exams.domain.model.RespuestaAlumno

interface AlumnoRepository {
    suspend fun getAllAlumnos(): List<Alumno>
    suspend fun getAlumnoById(id: String): Alumno?
    suspend fun getAlumnoByEmail(email: String): Alumno?
    suspend fun insertAlumno(alumno: Alumno)
    suspend fun updateAlumno(alumno: Alumno)
    suspend fun deleteAlumno(id: String)
    suspend fun countAlumnos(): Long
}

interface ExamenRepository {
    suspend fun getAllExamenes(): List<Examen>
    suspend fun getExamenesActivos(): List<Examen>
    suspend fun getExamenById(id: String): Examen?
    suspend fun insertExamen(examen: Examen)
    suspend fun updateExamen(examen: Examen)
    suspend fun deleteExamen(id: String)
}

interface ResultadoRepository {
    suspend fun getResultadosByAlumno(alumnoId: String): List<Resultado>
    suspend fun getResultadosByExamen(examenId: String): List<Resultado>
    suspend fun getResultadoById(id: String): Resultado?
    suspend fun insertResultado(resultado: Resultado, respuestas: List<RespuestaAlumno>)
    suspend fun alumnoYaTomoPrueba(alumnoId: String, examenId: String): Boolean
    suspend fun getRespuestasByResultado(resultadoId: String): List<RespuestaAlumno>
    suspend fun promedioCalificacionExamen(examenId: String): Double?
    suspend fun countResultadosByExamen(examenId: String): Long
}
