@file:Suppress("unused")

package data.databases

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://ep-cold-wind-aqagg94b.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require"
            driverClassName = "org.postgresql.Driver"
            username = "neondb_owner"
            password = "npg_dbaT4c3mKvIV"
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
                DebtsTable,
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

