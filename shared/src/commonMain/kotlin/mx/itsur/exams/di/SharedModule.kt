package mx.itsur.exams.di

import mx.itsur.exams.data.local.createDatabase
import mx.itsur.exams.data.repository.AlumnoRepositoryImpl
import mx.itsur.exams.data.repository.ExamenRepositoryImpl
import mx.itsur.exams.data.repository.ResultadoRepositoryImpl
import mx.itsur.exams.domain.usecase.*
import mx.itsur.exams.presentation.viewmodel.*
import org.koin.dsl.module

val sharedModule = module {
    single { createDatabase(get()) }

    single { AlumnoRepositoryImpl(get()) }
    single { ExamenRepositoryImpl(get()) }
    single { ResultadoRepositoryImpl(get()) }

    factory { LoginUseCase(get<AlumnoRepositoryImpl>()) }
    factory { GetExamenesUseCase(get<ExamenRepositoryImpl>()) }
    factory { GetExamenByIdUseCase(get<ExamenRepositoryImpl>()) }
    factory { CrearExamenUseCase(get<ExamenRepositoryImpl>()) }
    factory { ActualizarExamenUseCase(get<ExamenRepositoryImpl>()) }
    factory { EliminarExamenUseCase(get<ExamenRepositoryImpl>()) }
    factory { CalificarExamenUseCase() }
    factory { GetAlumnosUseCase(get<AlumnoRepositoryImpl>()) }
    factory { RegistrarAlumnoUseCase(get<AlumnoRepositoryImpl>()) }
    factory { EliminarAlumnoUseCase(get<AlumnoRepositoryImpl>()) }
    factory { GetResultadosByAlumnoUseCase(get<ResultadoRepositoryImpl>()) }
    factory { GetResultadosByExamenUseCase(get<ResultadoRepositoryImpl>()) }
    factory { GuardarResultadoUseCase(get<ResultadoRepositoryImpl>()) }
    factory { AlumnoYaTomoPruebaUseCase(get<ResultadoRepositoryImpl>()) }

    factory { LoginViewModel(get()) }
    factory { ExamenListViewModel(get(), get()) }
    factory { CrearExamenViewModel(get(), get(), get()) }
    factory { AplicarExamenViewModel(get(), get(), get(), get()) }
    factory { AlumnoListViewModel(get(), get(), get()) }
    factory { ReportesViewModel(get(), get(), get()) }
}
