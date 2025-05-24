package com.example.sportoapppit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileStats {

    data class ProfileStat(
        val date: String = "",
        val weight: Double = 0.0,
        val height: Double = 0.0,
        val bmi: Double = 0.0,
        val fatPercent: Double = 0.0
    )


// 2. RecyclerView Adapter for the stats history

    class ProfileStatAdapter(private val stats: List<ProfileStat>) :
        RecyclerView.Adapter<ProfileStatAdapter.StatViewHolder>() {

        class StatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.tvStatDate)
            val tvDetails: TextView = itemView.findViewById(R.id.tvStatDetails)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_profile_stat, parent, false)
            return StatViewHolder(view)
        }

        override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
            val stat = stats[position]
            holder.tvDate.text = stat.date
            holder.tvDetails.text = "🧓 ${stat.weight} kg    📏 ${stat.height} cm    BMI: ${String.format("%.1f", stat.bmi)}    Fat: ${String.format("%.1f", stat.fatPercent)}%"
        }

        override fun getItemCount(): Int = stats.size
    }
}