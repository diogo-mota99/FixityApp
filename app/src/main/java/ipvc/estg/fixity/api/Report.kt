package ipvc.estg.fixity.api

import java.sql.Blob

data class Report(
    val id: Int,
    val problem: String,
    val latitude: Double,
    val longitude: Double,
    val photo: Blob,
    val user_id: Int,
    val problemType_id: Int
)