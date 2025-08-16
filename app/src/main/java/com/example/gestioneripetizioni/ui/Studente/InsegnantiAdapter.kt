package com.example.gestioneripetizioni.ui.Studente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.model.Insegnante

//Adapter per la RecyclerView, mostra una lista di insegnanti
class InsegnantiAdapter (private val onInsegnanteClick: (Insegnante) -> Unit) :
    RecyclerView.Adapter<InsegnantiAdapter.InsegnanteViewHolder>() {

     //Lista che contiene i dati degli insegnanti da mostrare
    private var insegnanti = listOf<Insegnante>()


    fun aggiornaInsegnanti(nuoviInsegnanti: List<Insegnante>) {
        insegnanti = nuoviInsegnanti

        //Notifica alla recyclerView che il dataset è cambiato
        notifyDataSetChanged()
    }

    //Il ViewHolder rappresenta un singolo item della lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsegnanteViewHolder {

        //Crea la vista per un singolo item usando il layout passato
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_insegnante, parent, false)
        return InsegnanteViewHolder(view)
    }

    //Collega i dati dell'insegnante e alla vista del ViewHolder
    override fun onBindViewHolder(holder: InsegnanteViewHolder, position: Int) {
        holder.bind(insegnanti[position])
    }

    //Usa questo metodo per sapere quanti item deve disegnare
    override fun getItemCount() = insegnanti.size



    //Contiene i riferimenti agli elementi della UI per un singolo item e li riempie con i dati dell'insegnante
    inner class InsegnanteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        private val tvMaterie: TextView = itemView.findViewById(R.id.tvMaterie)
        private val tvOrari: TextView = itemView.findViewById(R.id.tvOrari)
        private val tvContatti: TextView = itemView.findViewById(R.id.tvContatti)

        fun bind(insegnante: Insegnante) {
            tvNome.text = "${insegnante.nome} ${insegnante.cognome}"
            tvMaterie.text = "Materie: ${insegnante.materie.joinToString(", ")}"
            tvOrari.text = "Orari: ${insegnante.orari.joinToString(", ")}"
            tvContatti.text = "${insegnante.email} | ${insegnante.telefono}"

            //Imposta un Listener per il click sull'item della lista
            itemView.setOnClickListener {

                //Quando viene cliccato esegue la funzione onInsegnanteClick per l'insegnante specificato come argomento
                onInsegnanteClick(insegnante)
            }
        }
    }
}
