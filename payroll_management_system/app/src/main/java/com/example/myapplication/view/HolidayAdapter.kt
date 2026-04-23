package com.example.myapplication.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.Holiday

class HolidayAdapter(private var holidayList: List<Holiday>) :
    RecyclerView.Adapter<HolidayAdapter.HolidayViewHolder>() {

    class HolidayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_holiday, parent, false)
        return HolidayViewHolder(view)
    }

    override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
        val holiday = holidayList[position]
        holder.tvDate.text = holiday.date
        holder.tvName.text = holiday.name
        holder.tvLocation.text = holiday.location
        holder.tvType.text = holiday.type
        
        // Color coding for Type
        when (holiday.type) {
            "H" -> holder.tvType.setTextColor("#2051E5".toColorInt())
            "OH" -> holder.tvType.setTextColor("#757575".toColorInt())
            "MH" -> holder.tvType.setTextColor("#E91E63".toColorInt())
        }
    }

    override fun getItemCount(): Int = holidayList.size

    fun updateList(newList: List<Holiday>) {
        holidayList = newList
        notifyDataSetChanged()
    }
}