package com.example.myapplication.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.CalendarDay

class CalendarAdapter(
    private val daysList: List<CalendarDay>,
    private val onDateClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        val dotStatus: View = view.findViewById(R.id.dotStatus)
        val llDayContainer: View = view.findViewById(R.id.llDayContainer)
        val ivWeekendIcon: View = view.findViewById(R.id.ivWeekendIcon)
        val tvWeekendLabel: View = view.findViewById(R.id.tvWeekendLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = daysList[position]
        val context = holder.itemView.context

        if (day.date == 0) {
            holder.itemView.visibility = View.INVISIBLE
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.tvDayNumber.text = day.date.toString()

            // Reset to default background and alpha
            holder.llDayContainer.setBackgroundResource(R.drawable.calendar_day_border_bg)
            holder.llDayContainer.alpha = 1.0f

            when (day.status) {
                "present" -> {
                    holder.dotStatus.visibility = View.VISIBLE
                    holder.dotStatus.setBackgroundResource(R.drawable.dot_present)
                    holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    holder.tvDayNumber.visibility = View.VISIBLE
                    holder.ivWeekendIcon.visibility = View.GONE
                    holder.tvWeekendLabel.visibility = View.GONE
                    holder.itemView.isClickable = true
                }
                "absent" -> {
                    holder.dotStatus.visibility = View.VISIBLE
                    holder.dotStatus.setBackgroundResource(R.drawable.dot_absent)
                    holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#F44336"))
                    holder.tvDayNumber.visibility = View.VISIBLE
                    holder.ivWeekendIcon.visibility = View.GONE
                    holder.tvWeekendLabel.visibility = View.GONE
                    holder.itemView.isClickable = true
                }
                "weekend" -> {
                    holder.dotStatus.visibility = View.INVISIBLE
                    holder.tvDayNumber.visibility = View.GONE
                    holder.ivWeekendIcon.visibility = View.GONE
                    holder.tvWeekendLabel.visibility = View.GONE
                    holder.itemView.isClickable = false
                    holder.itemView.isEnabled = false
                    
                    // Set the tropical island background and 100% visibility
                    holder.llDayContainer.setBackgroundResource(R.drawable.weekend_island)
                    holder.llDayContainer.alpha = 1.0f
                }
                else -> {
                    holder.dotStatus.visibility = View.INVISIBLE
                    holder.tvDayNumber.setTextColor(android.graphics.Color.parseColor("#000000"))
                    holder.tvDayNumber.visibility = View.VISIBLE
                    holder.ivWeekendIcon.visibility = View.GONE
                    holder.tvWeekendLabel.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (day.isCurrentMonth && day.status != "weekend") {
                    onDateClick(day.date)
                }
            }
        }
    }

    override fun getItemCount(): Int = daysList.size
}
