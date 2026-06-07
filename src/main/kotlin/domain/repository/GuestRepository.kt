package domain.repository

import domain.model.Subject

interface GuestRepository {
    suspend fun findAllSubjects(): List<Subject>
}