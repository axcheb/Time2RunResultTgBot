import org.flywaydb.core.Flyway
import org.sqlite.SQLiteDataSource
import ru.time2run.DB
import ru.time2run.runBot
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
    runBot(db)
}