package com.example.gestioneripetizioni.ui.Admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.model.Feedback

class AdminFeedbackAdapter(private val onFeedbackClick: (Feedback) -> Unit) :
    RecyclerView.Adapter<AdminFeedbackAdapter.FeedbackViewHolder>(){

    private var feedbacks = listOf<Feedback>()

    fun aggiornaFeedback(nuoviFeedbacks: List<Feedback> ){
        feedbacks=nuoviFeedbacks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_admin_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.bind(feedbacks[position])
    }

    override fun getItemCount() = feedbacks.size

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private val tvTesto: TextView= itemView.findViewById(R.id.tvTesto)
        private val tvAutore: TextView= itemView.findViewById(R.id.tvAutore)
        private val tvInsegnanteId: TextView= itemView.findViewById(R.id.tvInsegnanteId)

        fun bind(feedback: Feedback){
            tvTesto.text = feedback.testo ?: "[Testo non disponibile]"
            tvAutore.text= "Autore: ${feedback.autore ?: "Anonimo"}"
            tvInsegnanteId.text= "Insegnante ID : ${feedback.insegnanteId ?: "Sconosciuto"}"

            itemView.setOnClickListener {
                onFeedbackClick(feedback)
            }

        }

    }

}