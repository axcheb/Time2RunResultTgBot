package ru.time2run

import org.flywaydb.core.Flyway
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

val ds = SQLiteDataSource().apply {
    url = "jdbc:sqlite:time2run.db"
}

fun runMigrations(ds: DataSource) {
    Flyway.configure().dataSource(ds).load().migrate()
}

suspend fun main() {
    runMigrations(ds)
    val db = DB(ds)
    val bot = Time2RunBot(db)
    bot.runBot()
}