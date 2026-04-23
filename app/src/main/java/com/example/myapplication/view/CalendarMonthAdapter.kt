package com.example.myapplication.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.CalendarDay
import java.util.*

class CalendarMonthAdapter(
    private var data: Map<Int, List<CalendarDay>> = emptyMap(),
    private val onDateClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarMonthAdapter.MonthViewHolder>() {

    class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvMonth: RecyclerView = view.findViewById(R.id.rvMonth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_month, parent, false)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        // Position represents the month relative to some start point
        // For simplicity, we use the data map provided by the Activity
        val days = data[position] ?: emptyList()
        holder.rvMonth.layoutManager = GridLayoutManager(holder.itemView.context, 7)
        holder.rvMonth.adapter = CalendarAdapter(days, onDateClick)
    }

    override fun getItemCount(): Int = 2400 // Infinite-ish (200 years)

    fun updateData(newData: Map<Int, List<CalendarDay>>) {
        val updatedMap = data.toMutableMap()
        updatedMap.putAll(newData)
        this.data = updatedMap
        notifyDataSetChanged()
    }
}
