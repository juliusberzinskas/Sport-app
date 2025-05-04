package com.example.sportoapppit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

// Vienos dienos duomenų modelis
data class CalendarDay(
    val date: LocalDate,
    val isToday: Boolean = false,
    val hasWorkout: Boolean = false
)

class CalendarDayAdapter(
    private val days: List<CalendarDay>,
    private val onDaySelected: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder>() {

    inner class CalendarDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayShort: TextView = itemView.findViewById(R.id.home_page_DayShort)
        val tvDayNumber: TextView = itemView.findViewById(R.id.home_page_DayNumber)

        fun bind(day: CalendarDay, position: Int) {
            tvDayShort.text = day.date.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale("lt"))
                .uppercase().take(2)

            tvDayNumber.text = day.date.dayOfMonth.toString()

            itemView.setOnClickListener {
                onDaySelected(day.date)
            }

            // Pasirink foną pagal loginę sąlygą
            val backgroundRes = when {
                day.isToday -> R.drawable.bg_calendar_day_today
                day.hasWorkout -> R.drawable.bg_calendar_day_highlighted
                else -> R.drawable.bg_calendar_day
            }

            itemView.setBackgroundResource(backgroundRes)

            // Jei šiandien – pakeičiam tekstą į kontrastingą
            val textColor = if (day.isToday) android.R.color.white else android.R.color.white
            tvDayShort.setTextColor(ContextCompat.getColor(itemView.context, textColor))
            tvDayNumber.setTextColor(ContextCompat.getColor(itemView.context, textColor))

            // Išjungiam visus paspaudimų efektus
            itemView.isPressed = false
            itemView.isSelected = false
            itemView.isActivated = false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        holder.bind(days[position], position)
    }

    override fun getItemCount(): Int = days.size
}
