package com.example.sportoapppit

import java.io.Serializable

data class WorkoutSession(
    val type: String, // "bėgimas" arba "ėjimas"
    val dateTime: String, // pvz. 2025-05-04 18:42
    val steps: Int,
    val distanceKm: Double,
    val durationSec: Int,
    val calories: Int,
    val mapImagePath: String? = null
) : Serializable
