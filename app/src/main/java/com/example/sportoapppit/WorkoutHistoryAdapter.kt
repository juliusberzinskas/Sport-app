package com.example.sportoapppit

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutHistoryAdapter(
    private val groupedSessions: Map<String, List<WorkoutSession>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()

    init {
        groupedSessions.forEach { (date, sessions) ->
            items.add(date)
            items.addAll(sessions)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_date, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_session, parent, false)
            SessionViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder) {
            holder.tvDate.text = items[position] as String
        } else if (holder is SessionViewHolder) {
            val session = items[position] as WorkoutSession
            holder.tvType.text = session.type.replaceFirstChar { it.uppercaseChar() }
            holder.tvDistance.text = String.format("%.2f", session.distanceKm)
            holder.tvDuration.text = (session.durationSec / 60).toString()
            holder.tvCalories.text = session.calories.toString()
            holder.tvTime.text = session.dateTime.substringAfter(" ")

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, WorkoutDetailsActivity::class.java)
                intent.putExtra("workout", session)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    class SessionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvCalories: TextView = view.findViewById(R.id.tvCalories)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }
}
