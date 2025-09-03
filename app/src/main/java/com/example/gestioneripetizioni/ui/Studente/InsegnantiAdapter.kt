package com.example.gestioneripetizioni.ui.Studente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.model.Insegnante

class InsegnantiAdapter(
    //definizione delle tre azioni che possono essere eseguite
    private val onEmailClick: (Insegnante) -> Unit,
    private val onCallClick: (Insegnante) -> Unit,
    private val onFeedbackClick: (Insegnante) -> Unit
) : RecyclerView.Adapter<InsegnantiAdapter.InsegnanteViewHolder>() {

    //lista contenente i dati degli insegnanti da visualizzare
    private var insegnanti = listOf<Insegnante>()

    //metodo per l'aggiornamento della lista
    fun aggiornaInsegnanti(nuoviInsegnanti: List<Insegnante>) {
        insegnanti = nuoviInsegnanti
        notifyDataSetChanged()
    }

    //metodo per la creazione di nuove 'righe' per permettere una buona visualizzazione
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsegnanteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_insegnante, parent, false)
        return InsegnanteViewHolder(view)
    }

    //metodo chiamato per mostrare a schermo una nuova 'riga'
    override fun onBindViewHolder(holder: InsegnanteViewHolder, position: Int) {
        holder.bind(insegnanti[position])
    }

    //determina quante righe devono essere visualizzate a schermo
    override fun getItemCount() = insegnanti.size

    //classe che rappresenta una singola riga della lista
    inner class InsegnanteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        private val tvMaterie: TextView = itemView.findViewById(R.id.tvMaterie)
        private val tvOrari: TextView = itemView.findViewById(R.id.tvOrari)
        private val tvContatti: TextView = itemView.findViewById(R.id.tvContatti)
        private val btnEmail: Button = itemView.findViewById(R.id.btnEmail)
        private val btnChiama: Button = itemView.findViewById(R.id.btnChiama)
        private val btnFeedback: Button = itemView.findViewById(R.id.btnFeedback)

        //metodo per collegare i dati di un oggetto Insegnante alla riga
        fun bind(insegnante: Insegnante) {
            tvNome.text = "${insegnante.nome} ${insegnante.cognome}"
            tvMaterie.text = "Materie: ${insegnante.materie.joinToString(", ")}"
            tvOrari.text = "Orari: ${insegnante.orari.joinToString(", ")}"
            tvContatti.text = "${insegnante.email} | ${insegnante.telefono}"

            //gestione dei pulsanti per le tre azioni possibili
            btnEmail.setOnClickListener { onEmailClick(insegnante) }
            btnChiama.setOnClickListener { onCallClick(insegnante) }
            btnFeedback.setOnClickListener { onFeedbackClick(insegnante) }
        }
    }
}

