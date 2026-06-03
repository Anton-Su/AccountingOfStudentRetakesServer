package helpers

import data.databases.RetakeTeachersTable
import org.jetbrains.exposed.sql.selectAll

fun fetchTeacherIds(retakeId: Long): List<Long> =
    RetakeTeachersTable.selectAll()
        .where { RetakeTeachersTable.retakeId eq retakeId }
        .map { it[RetakeTeachersTable.teacherId].value }