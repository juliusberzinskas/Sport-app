import java.io.Serializable

data class WorkoutSession(
    val type: String = "",
    val dateTime: String = "",
    val steps: Int = 0,
    val distanceKm: Double = 0.0,
    val durationSec: Int = 0,
    val calories: Double = 0.0,
    val mapImagePath: String? = null
) : Serializable
