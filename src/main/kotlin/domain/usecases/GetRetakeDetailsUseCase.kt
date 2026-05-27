package domain.usecases


import domain.model.RetakeDetails
import domain.repository.StudentRepository



class GetRetakeDetailsUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(retakeId: Long): RetakeDetails {
        require(retakeId > 0) { "Retake ID must be positive" }
        val retake = studentRepository.findRetakeById(retakeId)
            ?: throw IllegalArgumentException("Retake with id $retakeId not found")
        val enrollments = studentRepository.findEnrollmentsByRetakeId(retakeId)
        return RetakeDetails(retake, enrollments)
    }
}
