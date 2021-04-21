package ipvc.estg.fixity.api

import android.media.Image
import java.sql.Blob

data class Report(
    val id: Int,
    val problem: String,
    val latitude: Double,
    val longitude: Double,
    val image_path: String,
    val user_id: Int,
    val problemType_id: Int
)