package com.example.sportoapppit

sealed class WorkoutHistoryItem {
    data class DateHeader(val label: String) : WorkoutHistoryItem()
    data class SessionItem(val session: WorkoutSession) : WorkoutHistoryItem()
}
