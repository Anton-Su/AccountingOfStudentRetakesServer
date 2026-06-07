package data.databases

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    val DB_URL = System.getenv("DB_URL") ?: error("DB_URL не задан")
    val DB_USER = System.getenv("DB_USER") ?: error("DB_USER не задан")
    val DB_PASSWORD = System.getenv("DB_PASSWORD") ?: error("DB_PASSWORD не задан")
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = DB_URL
            driverClassName = "org.postgresql.Driver"
            username = DB_USER
            password = DB_PASSWORD
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                StudentsTable,
                TeachersTable,
                TeacherDisciplinesTable,
                SubjectsTable,
                StudentSubjectsTable,
                RetakesTable,
                RetakeTeachersTable,
                RetakeEnrollmentsTable,
                GradesTable,
                CommentsTable
            )
            DatabaseSeeder.seed()
        }
        println("PostgreSQL (Neon) подключён успешно")
    }
}

