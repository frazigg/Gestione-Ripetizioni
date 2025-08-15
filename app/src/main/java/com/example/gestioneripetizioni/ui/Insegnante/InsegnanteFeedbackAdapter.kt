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

    /* Chiamato quando la RecyclerView ha bisogno di creare una nuova vista
    per rappresentare un elemento della lista */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback,parent,false)
        return FeedbackViewHolder(view)
    }

    //Chiamato dalla RecyclerView per visualizzare i dati nella posizione specificata
    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.bind(feedback)
    }

    //Restituisce il numero totale di elementi presenti nella lista
    override fun getItemCount(): Int {
        return feedbackList.size
    }

    //Aggiorna la lista di feedback mostrata dall'adapter
    fun aggiornaFeedback(newFeedbackList : List<Feedback>){

        //Filtra la nuova lista per includere solo gli elementi considerati validi
            val safeFeedbackList = newFeedbackList.filter { it.isValid() }
            feedbackList = safeFeedbackList

        //Notifica alla RecyclerView che i dati sono cambiati
            notifyDataSetChanged()

    }

    //Contiene i riferimenti ai componenti UI di un elemento della lista
    class FeedbackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        //Riferimenti alle TextView definite nel file di layout
        private val testoFeedback: TextView = itemView.findViewById(R.id.testoFeedback)
        private val autoreFeedback: TextView = itemView.findViewById(R.id.autoreFeedback)

        fun bind(feedback: Feedback){

            //Imposta il testo del feedback e dell'autore nelle rispettive TextView
                testoFeedback.text = feedback.getSafeTesto()
                autoreFeedback.text = "- ${feedback.getSafeAutore()}"
        }
    }
}