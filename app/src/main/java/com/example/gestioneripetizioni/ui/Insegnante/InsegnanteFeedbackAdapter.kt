package com.example.gestioneripetizioni.ui.Insegnante

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.model.Feedback
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.gestioneripetizioni.R


class InsegnanteFeedbackAdapter(private var feedbackList: List<Feedback>):
    RecyclerView.Adapter<InsegnanteFeedbackAdapter.FeedbackViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback,parent,false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.bind(feedback)
    }

    override fun getItemCount(): Int {
        return feedbackList.size
    }

    fun aggiornaFeedback(newFeedbackList : List<Feedback>){
        try{
            val safeFeedbackList = newFeedbackList.filter { it != null && it.isValid() }
            feedbackList = safeFeedbackList

            if(hasObservers()){
                notifyDataSetChanged()
            }
        }catch (e: Exception){
            println("DEBUG: Errore nell'aggiornamento della lista feedback: ${e.message}")
            feedbackList = emptyList()
            if(hasObservers()){
                notifyDataSetChanged()
            }
        }
    }

    class FeedbackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val testoFeedback: TextView = itemView.findViewById(R.id.testoFeedback)
        private val autoreFeedback: TextView = itemView.findViewById(R.id.autoreFeedback)

        fun bind(feedback: Feedback){
            println("DEBUG: Binding feedback: ${feedback.id}")
            try{
                val testo = feedback.getSafeTesto()
                val autore = feedback.getSafeAutore()
                println("DEBUG: Testo: $testo, Autore: $autore")

                testoFeedback.text = testo
                autoreFeedback.text = "- $autore"
                println("DEBUG: Binging completato con successo")
            } catch (e: Exception){
              println("DEBUG: Errore nel binding del feedback: ${e.message}")
              e.printStackTrace()
              testoFeedback.text = "[Errore nel caricamento del feedback]"
              autoreFeedback.text = "- Anonimo"
            }
        }
    }
}