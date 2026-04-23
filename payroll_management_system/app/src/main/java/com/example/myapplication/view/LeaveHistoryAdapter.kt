package com.example.myapplication.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.LeaveResponse

class LeaveHistoryAdapter(private val historyList: List<LeaveResponse>) :
    RecyclerView.Adapter<LeaveHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeaveType: TextView = view.findViewById(R.id.tvLeaveType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDateRange: TextView = view.findViewById(R.id.tvDateRange)
        val tvReason: TextView = view.findViewById(R.id.tvReason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leave_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.tvLeaveType.text = item.leaveType ?: "N/A"
        holder.tvDateRange.text = holder.itemView.context.getString(
            R.string.date_range_format, 
            item.startDate ?: "N/A", 
            item.endDate ?: "N/A"
        )
        holder.tvReason.text = holder.itemView.context.getString(
            R.string.reason_format, 
            item.description ?: item.reason ?: ""
        )
        
        val status = (item.status ?: "PENDING").uppercase()
        holder.tvStatus.text = status

        when (status) {
            "APPROVED" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.status_approved_bg)
                holder.tvStatus.setTextColor("#4CAF50".toColorInt())
            }
            "REJECTED" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.status_rejected_bg)
                holder.tvStatus.setTextColor("#F44336".toColorInt())
            }
            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg)
                holder.tvStatus.setTextColor("#FF9800".toColorInt())
            }
        }
    }

    override fun getItemCount() = historyList.size
}