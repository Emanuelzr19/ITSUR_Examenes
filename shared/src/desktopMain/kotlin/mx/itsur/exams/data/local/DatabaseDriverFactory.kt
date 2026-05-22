package mx.itsur.exams.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import mx.itsur.exams.db.ITSURDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbDir = File(System.getProperty("user.home"), ".itsur_exams")
        dbDir.mkdirs()
        val dbPath = File(dbDir, "itsur_exams.db")
        val isNew = !dbPath.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath.absolutePath}")
        if (isNew) {
            ITSURDatabase.Schema.create(driver)
        }
        return driver
    }
}
