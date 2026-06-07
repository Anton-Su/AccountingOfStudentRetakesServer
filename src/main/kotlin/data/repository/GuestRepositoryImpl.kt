package data.repository

import data.databases.SubjectsTable
import data.mappers.toSubject
import domain.model.Subject
import domain.repository.GuestRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class GuestRepositoryImpl : GuestRepository {
    override suspend fun findAllSubjects(): List<Subject> = transaction {
        SubjectsTable.selectAll().map { it.toSubject() }
    }
}