package mx.itsur.exams.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import mx.itsur.exams.db.ITSURDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(ITSURDatabase.Schema, context, "itsur_exams.db")
    }
}
