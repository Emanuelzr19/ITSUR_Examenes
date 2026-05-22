package mx.itsur.exams

import android.app.Application
import mx.itsur.exams.data.local.DatabaseDriverFactory
import mx.itsur.exams.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ITSURApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ITSURApp)
            modules(
                module { single { DatabaseDriverFactory(androidContext()) } },
                sharedModule
            )
        }
    }
}
