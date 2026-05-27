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
            jdbcUrl = "jdbc:postgresql://ep-noisy-sun-aqf5fddp.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require"
            driverClassName = "org.postgresql.Driver"
            username = "neondb_owner"
            password = "npg_y6gOZYwKQ0cV"
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
                SubjectsTable,
                DebtsTable,
                RetakesTable,
                RetakeTeachersTable,
                RetakeEnrollmentsTable,
                GradesTable,
                SubjectStudentsTable
            )
            DatabaseSeeder.seed()
        }
        println("PostgreSQL (Neon) подключён успешно")
    }
}