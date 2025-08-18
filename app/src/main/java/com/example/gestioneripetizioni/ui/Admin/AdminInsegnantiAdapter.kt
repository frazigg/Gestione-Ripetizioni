package com.example.gestioneripetizioni.ui.Admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.FabPosition
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.model.Insegnante
import com.example.gestioneripetizioni.ui.Studente.InsegnantiAdapter

class AdminInsegnantiAdapter(private val onInsegnanteClick: (Insegnante) -> Unit ) :
    RecyclerView.Adapter<AdminInsegnantiAdapter.InsegnanteViewHolder>(){

    private var insegnanti = listOf<Insegnante>()

    fun aggiornaInsegnanti(nuoviInsegnanti: List<Insegnante>){
        insegnanti = nuoviInsegnanti
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): AdminInsegnantiAdapter.InsegnanteViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.item_admin_insegnante, parent, false)
        return InsegnanteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsegnanteViewHolder, position: Int) {
        holder.bind(insegnanti[position])
    }

    override fun getItemCount() = insegnanti.size

    inner class InsegnanteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        private val tvMaterie: TextView = itemView.findViewById(R.id.tvMaterie)
        private val tvOrari: TextView = itemView.findViewById(R.id.tvOrari)

        fun bind(insegnante: Insegnante){
            tvNome.text = "${insegnante.nome} ${insegnante.cognome} (ID: ${insegnante.id}) "
            tvMaterie.text = "Materie: ${insegnante.materie.joinToString(", ")}"
            tvOrari.text = "Orari: ${insegnante.orari.joinToString(", ")}"

            itemView.setOnClickListener {
                onInsegnanteClick(insegnante)
            }
        }
    }



}