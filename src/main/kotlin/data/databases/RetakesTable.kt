package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object RetakesTable : LongIdTable("retakes") {
    val type = varchar("type", 255)
    val admission = varchar("admission", 255).nullable()
    val startAt = long("start_at")
    val endAt = long("end_at")
}