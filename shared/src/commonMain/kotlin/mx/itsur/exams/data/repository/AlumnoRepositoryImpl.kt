package mx.itsur.exams.data.repository

import mx.itsur.exams.data.local.mapToAlumno
import mx.itsur.exams.db.ITSURDatabase
import mx.itsur.exams.domain.model.Alumno
import mx.itsur.exams.domain.repository.AlumnoRepository

class AlumnoRepositoryImpl(private val database: ITSURDatabase) : AlumnoRepository {

    private val queries = database.iTSURDatabaseQueries

    override suspend fun getAllAlumnos(): List<Alumno> =
        queries.selectAllAlumnos().executeAsList().map {
            mapToAlumno(it.id, it.nombre, it.email, it.numero_control, it.grupo, it.rol, it.password_hash, it.fecha_registro)
        }

    override suspend fun getAlumnoById(id: String): Alumno? =
        queries.selectAlumnoById(id).executeAsOneOrNull()?.let {
            mapToAlumno(it.id, it.nombre, it.email, it.numero_control, it.grupo, it.rol, it.password_hash, it.fecha_registro)
        }

    override suspend fun getAlumnoByEmail(email: String): Alumno? =
        queries.selectAlumnoByEmail(email).executeAsOneOrNull()?.let {
            mapToAlumno(it.id, it.nombre, it.email, it.numero_control, it.grupo, it.rol, it.password_hash, it.fecha_registro)
        }

    override suspend fun insertAlumno(alumno: Alumno) {
        queries.insertAlumno(
            id = alumno.id,
            nombre = alumno.nombre,
            email = alumno.email,
            numero_control = alumno.numeroControl,
            grupo = alumno.grupo,
            rol = alumno.rol.name,
            password_hash = alumno.passwordHash,
            fecha_registro = alumno.fechaRegistro
        )
    }

    override suspend fun updateAlumno(alumno: Alumno) {
        queries.updateAlumno(
            nombre = alumno.nombre,
            email = alumno.email,
            numero_control = alumno.numeroControl,
            grupo = alumno.grupo,
            id = alumno.id
        )
    }

    override suspend fun deleteAlumno(id: String) {
        queries.deleteAlumno(id)
    }

    override suspend fun countAlumnos(): Long =
        queries.countAlumnos().executeAsOne()
}
