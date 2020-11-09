package com.hebreuyannis.pocksmsscheduler.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hebreuyannis.pocksmsscheduler.R
import com.hebreuyannis.pocksmsscheduler.model.TransactionPresentation
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val itemClickListener: (TransactionPresentation) -> Unit) :
    ListAdapter<TransactionPresentation, TransactionAdapter.ViewHolder>(
        DiffCallback
    ) {

    companion object {
        @JvmStatic
        val DiffCallback = object : DiffUtil.ItemCallback<TransactionPresentation>() {
            override fun areItemsTheSame(
                oldItem: TransactionPresentation,
                newItem: TransactionPresentation
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: TransactionPresentation,
                newItem: TransactionPresentation
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    data class ViewHolder(val root: View) :
        RecyclerView.ViewHolder(root) {
        private val dateText: TextView = root.findViewById(R.id.title1)
        private val nameText = root.findViewById<TextView>(R.id.title2)
        private val statusText: TextView = root.findViewById(R.id.title3)

        fun bind(
            transactionPresentation: TransactionPresentation,
            clickListener: (TransactionPresentation) -> Unit
        ) {
            itemView.setOnClickListener { clickListener(transactionPresentation) }
            dateText.text = converteDate(transactionPresentation.dateCreate)
            nameText.text = transactionPresentation.name
            statusText.text = setStatus(transactionPresentation.status)
        }

        private fun converteDate(dateTime: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateTime
            val date = calendar.time
            val format = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            return format.format(date)
        }

        private fun setStatus(status: Int): String {
            return when (status) {
                -1 -> "untreated"
                0 -> "send"
                4 -> "failed/service unavailable"
                else -> "Failed"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.contacts_list_item, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }
}