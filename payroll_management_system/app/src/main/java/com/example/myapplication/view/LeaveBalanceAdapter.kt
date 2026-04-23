package com.example.myapplication.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.LeaveBalanceItem
import com.example.myapplication.data.model.LeaveBalanceResponse

class LeaveBalanceAdapter(private val items: List<Any>) :
    RecyclerView.Adapter<LeaveBalanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeaveType: TextView = view.findViewById(R.id.tvLeaveType)
        val tvUsedStatus: TextView = view.findViewById(R.id.tvUsedStatus)
        val tvTotalStatus: TextView = view.findViewById(R.id.tvTotalStatus)
        val progressBar: ProgressBar = view.findViewById(R.id.pbLeave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leave_balance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        when (item) {
            is LeaveBalanceResponse -> {
                // EMS API Response - show all leave types
                if (position == 0) {
                    holder.tvLeaveType.text = "Casual Leave"
                    holder.tvUsedStatus.text = "Available: ${item.casualLeave ?: 0}"
                    val total = item.totalLeave ?: 0
                    val casual = item.casualLeave ?: 0
                    val used = total - casual
                    holder.tvTotalStatus.text = "$casual of $total Available"
                    
                    val progress = if (total > 0) {
                        ((casual.toDouble() / total) * 100).toInt()
                    } else 0
                    holder.progressBar.progress = progress
                } else if (position == 1) {
                    holder.tvLeaveType.text = "Sick Leave"
                    holder.tvUsedStatus.text = "Available: ${item.sickLeave ?: 0}"
                    val total = item.totalLeave ?: 0
                    val sick = item.sickLeave ?: 0
                    holder.tvTotalStatus.text = "$sick of $total Available"
                    
                    val progress = if (total > 0) {
                        ((sick.toDouble() / total) * 100).toInt()
                    } else 0
                    holder.progressBar.progress = progress
                } else if (position == 2) {
                    holder.tvLeaveType.text = "Earned Leave"
                    holder.tvUsedStatus.text = "Available: ${item.earnedLeave ?: 0}"
                    val total = item.totalLeave ?: 0
                    val earned = item.earnedLeave ?: 0
                    holder.tvTotalStatus.text = "$earned of $total Available"
                    
                    val progress = if (total > 0) {
                        ((earned.toDouble() / total) * 100).toInt()
                    } else 0
                    holder.progressBar.progress = progress
                }
            }
            is LeaveBalanceItem -> {
                // Legacy PMS API Response
                holder.tvLeaveType.text = item.leaveType
                holder.tvUsedStatus.text = "Used: ${item.usedLeaves}"
                holder.tvTotalStatus.text = "${item.usedLeaves} of ${item.totalLeaves} Used"
                
                val progress = if (item.totalLeaves > 0) {
                    ((item.usedLeaves / item.totalLeaves) * 100).toInt()
                } else 0
                holder.progressBar.progress = progress
            }
        }
    }

    override fun getItemCount() = items.size
}
