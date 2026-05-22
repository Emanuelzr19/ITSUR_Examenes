package mx.itsur.exams.data.repository

import mx.itsur.exams.data.local.*
import mx.itsur.exams.db.ITSURDatabase
import mx.itsur.exams.domain.model.Examen
import mx.itsur.exams.domain.model.Pregunta
import mx.itsur.exams.domain.repository.ExamenRepository

class ExamenRepositoryImpl(private val database: ITSURDatabase) : ExamenRepository {

    private val queries = database.iTSURDatabaseQueries

    override suspend fun getAllExamenes(): List<Examen> {
        return queries.selectAllExamenes().executeAsList().map { e ->
            val preguntas = loadPreguntas(e.id)
            mapToExamen(e.id, e.titulo, e.descripcion, e.duracion_minutos, e.fecha_creacion, e.creado_por, e.activo).copy(preguntas = preguntas)
        }
    }

    override suspend fun getExamenesActivos(): List<Examen> {
        return queries.selectExamenesActivos().executeAsList().map { e ->
            val preguntas = loadPreguntas(e.id)
            mapToExamen(e.id, e.titulo, e.descripcion, e.duracion_minutos, e.fecha_creacion, e.creado_por, e.activo).copy(preguntas = preguntas)
        }
    }

    override suspend fun getExamenById(id: String): Examen? {
        val e = queries.selectExamenById(id).executeAsOneOrNull() ?: return null
        val preguntas = loadPreguntas(id)
        return mapToExamen(e.id, e.titulo, e.descripcion, e.duracion_minutos, e.fecha_creacion, e.creado_por, e.activo).copy(preguntas = preguntas)
    }

    private fun loadPreguntas(examenId: String): List<Pregunta> {
        return queries.selectPreguntasByExamen(examenId).executeAsList().map { p ->
            val opciones = queries.selectOpcionesByPregunta(p.id).executeAsList().map { o ->
                mapToOpcion(o.id, o.pregunta_id, o.texto, o.es_correcta)
            }
            mapToPregunta(p.id, p.examen_id, p.texto, p.tipo, p.puntaje, p.orden).copy(opciones = opciones)
        }
    }

    override suspend fun insertExamen(examen: Examen) {
        queries.transaction {
            queries.insertExamen(
                id = examen.id,
                titulo = examen.titulo,
                descripcion = examen.descripcion,
                duracion_minutos = examen.duracionMinutos.toLong(),
                fecha_creacion = examen.fechaCreacion,
                creado_por = examen.creadoPor,
                activo = if (examen.activo) 1L else 0L
            )
            examen.preguntas.forEach { pregunta ->
                queries.insertPregunta(
                    id = pregunta.id,
                    examen_id = pregunta.examenId,
                    texto = pregunta.texto,
                    tipo = pregunta.tipo.name,
                    puntaje = pregunta.puntaje.toDouble(),
                    orden = pregunta.orden.toLong()
                )
                pregunta.opciones.forEach { opcion ->
                    queries.insertOpcion(
                        id = opcion.id,
                        pregunta_id = opcion.preguntaId,
                        texto = opcion.texto,
                        es_correcta = if (opcion.esCorrecta) 1L else 0L
                    )
                }
            }
        }
    }

    override suspend fun updateExamen(examen: Examen) {
        queries.transaction {
            queries.updateExamen(
                titulo = examen.titulo,
                descripcion = examen.descripcion,
                duracion_minutos = examen.duracionMinutos.toLong(),
                activo = if (examen.activo) 1L else 0L,
                id = examen.id
            )
            queries.deletePreguntasByExamen(examen.id)
            examen.preguntas.forEach { pregunta ->
                queries.insertPregunta(
                    id = pregunta.id,
                    examen_id = pregunta.examenId,
                    texto = pregunta.texto,
                    tipo = pregunta.tipo.name,
                    puntaje = pregunta.puntaje.toDouble(),
                    orden = pregunta.orden.toLong()
                )
                pregunta.opciones.forEach { opcion ->
                    queries.insertOpcion(
                        id = opcion.id,
                        pregunta_id = opcion.preguntaId,
                        texto = opcion.texto,
                        es_correcta = if (opcion.esCorrecta) 1L else 0L
                    )
                }
            }
        }
    }

    override suspend fun deleteExamen(id: String) {
        queries.deleteExamen(id)
    }
}
