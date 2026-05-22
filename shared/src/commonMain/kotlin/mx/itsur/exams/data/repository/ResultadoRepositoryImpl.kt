package mx.itsur.exams.data.repository

import mx.itsur.exams.data.local.mapToResultado
import mx.itsur.exams.data.local.mapToRespuesta
import mx.itsur.exams.db.ITSURDatabase
import mx.itsur.exams.domain.model.Resultado
import mx.itsur.exams.domain.model.RespuestaAlumno
import mx.itsur.exams.domain.repository.ResultadoRepository

class ResultadoRepositoryImpl(private val database: ITSURDatabase) : ResultadoRepository {

    private val queries = database.iTSURDatabaseQueries

    override suspend fun getResultadosByAlumno(alumnoId: String): List<Resultado> =
        queries.selectResultadosByAlumno(alumnoId).executeAsList().map {
            mapToResultado(
                it.id, it.alumno_id, it.examen_id, it.calificacion,
                it.tiempo_usado_segundos, it.fecha_aplicacion, it.examen_titulo
            )
        }

    override suspend fun getResultadosByExamen(examenId: String): List<Resultado> =
        queries.selectResultadosByExamen(examenId).executeAsList().map {
            mapToResultado(
                it.id, it.alumno_id, it.examen_id, it.calificacion,
                it.tiempo_usado_segundos, it.fecha_aplicacion,
                alumnoNombre = it.alumno_nombre, numeroControl = it.numero_control
            )
        }

    override suspend fun getResultadoById(id: String): Resultado? =
        queries.selectResultadoById(id).executeAsOneOrNull()?.let {
            mapToResultado(it.id, it.alumno_id, it.examen_id, it.calificacion, it.tiempo_usado_segundos, it.fecha_aplicacion)
        }

    override suspend fun insertResultado(resultado: Resultado, respuestas: List<RespuestaAlumno>) {
        queries.transaction {
            queries.insertResultado(
                id = resultado.id,
                alumno_id = resultado.alumnoId,
                examen_id = resultado.examenId,
                calificacion = resultado.calificacion.toDouble(),
                tiempo_usado_segundos = resultado.tiempoUsadoSegundos.toLong(),
                fecha_aplicacion = resultado.fechaAplicacion
            )
            respuestas.forEach { r ->
                queries.insertRespuestaAlumno(
                    id = r.id,
                    resultado_id = r.resultadoId,
                    pregunta_id = r.preguntaId,
                    opcion_seleccionada_id = r.opcionSeleccionadaId,
                    respuesta_texto = r.respuestaTexto,
                    es_correcta = if (r.esCorrecta) 1L else 0L
                )
            }
        }
    }

    override suspend fun alumnoYaTomoPrueba(alumnoId: String, examenId: String): Boolean =
        queries.alumnoYaTomoPrueba(alumnoId, examenId).executeAsOne() > 0

    override suspend fun getRespuestasByResultado(resultadoId: String): List<RespuestaAlumno> =
        queries.selectRespuestasByResultado(resultadoId).executeAsList().map {
            mapToRespuesta(it.id, it.resultado_id, it.pregunta_id, it.opcion_seleccionada_id, it.respuesta_texto, it.es_correcta)
        }

    override suspend fun promedioCalificacionExamen(examenId: String): Double? =
        queries.promedioCalificacionExamen(examenId).executeAsOne().promedio

    override suspend fun countResultadosByExamen(examenId: String): Long =
        queries.countResultadosByExamen(examenId).executeAsOne()
}
