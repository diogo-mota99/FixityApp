package ipvc.estg.fixity.api

data class Report(
    val id: Int,
    val problem: String,
    val latitude: Double,
    val longitude: Double,
    val image_path: String,
    val timestamp: String,
    val problemType: Int,
    val user_id: Int,
)