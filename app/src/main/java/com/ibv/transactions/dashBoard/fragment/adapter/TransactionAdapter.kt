package com.ibv.transactions.dashBoard.fragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ibv.transactions.R
import com.ibv.transactions.base.DateAgo
import com.ibv.transactions.dashBoard.fragment.model.TransactionBean

class TransactionAdapter(private val transactions: List<TransactionBean>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val amount: TextView = itemView.findViewById(R.id.tvAmount)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false) // Ensure your item layout matches this ID
        return TransactionViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.date.text = DateAgo.getFormattedDate(transaction.date)
        holder.amount.text = transaction.amount.toString()
        holder.category.text = transaction.category
        holder.description.text = transaction.description
    }

    override fun getItemCount(): Int = transactions.size
}
