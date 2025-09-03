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

    //inizializzazione della lista che contiene i feedback
    private var feedbacks = listOf<Feedback>()

    //metodo per l'aggiornamento della lista
    fun aggiornaFeedback(nuoviFeedbacks: List<Feedback> ){
        //sostituzione della vecchia lista con quella nuova
        feedbacks=nuoviFeedbacks
        notifyDataSetChanged()
    }

    //metodo per la creazione di nuove 'righe' per permettere una buona visualizzazione
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_admin_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    //metodo chiamato per mostrare a schermo una nuova 'riga'
    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.bind(feedbacks[position])
    }

    //determina quante righe devono essere visualizzate a schermo
    override fun getItemCount() = feedbacks.size

    //classe che rappresenta una singola riga della lista
    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private val tvTesto: TextView= itemView.findViewById(R.id.tvTesto)
        private val tvAutore: TextView= itemView.findViewById(R.id.tvAutore)
        private val tvInsegnanteId: TextView= itemView.findViewById(R.id.tvInsegnanteId)

        //metodo per collegare i dati di un oggetto Feedback alla riga
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