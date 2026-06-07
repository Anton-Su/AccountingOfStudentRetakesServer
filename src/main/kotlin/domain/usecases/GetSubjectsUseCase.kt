package domain.usecases

import domain.model.Subject
import domain.repository.GuestRepository

class GetSubjectsUseCase(private val guestRepository: GuestRepository) {
    suspend operator fun invoke(): List<Subject> = guestRepository.findAllSubjects()
}

