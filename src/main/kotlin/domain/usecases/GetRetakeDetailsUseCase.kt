package domain.usecases

import domain.model.RetakeDetails
import domain.repository.StudentRepository
import domain.repository.TeacherRepository


class GetRetakeDetailsUseCase(
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
) {
    suspend operator fun invoke(retakeId: Long): RetakeDetails {
        require(retakeId > 0) { "Retake ID must be positive" }
        val retake = studentRepository.findRetakeById(retakeId)
            ?: throw IllegalArgumentException("Retake with id $retakeId not found")
        val enrollments = teacherRepository.findEnrollmentsByRetakeId(retakeId)
        return RetakeDetails(retake, enrollments)
    }
}
