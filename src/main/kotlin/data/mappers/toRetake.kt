package data.mappers

import data.databases.RetakesTable
import domain.model.Retake
import org.jetbrains.exposed.sql.ResultRow
import java.time.Instant

fun ResultRow.toRetake(teacherIds: List<Long>) = Retake(
    id = this[RetakesTable.id].value,
    type = this[RetakesTable.type],
    place = this[RetakesTable.place],
    admission = this[RetakesTable.admission],
    subjectId = this[RetakesTable.subjectId].value,
    startAt = Instant.ofEpochMilli(this[RetakesTable.startAt]),
    endAt = Instant.ofEpochMilli(this[RetakesTable.endAt]),
    lastModified = Instant.ofEpochMilli(this[RetakesTable.lastModified]),
    teacherIds = teacherIds
)
