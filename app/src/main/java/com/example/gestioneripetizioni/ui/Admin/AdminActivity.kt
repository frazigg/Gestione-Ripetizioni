package com.example.gestioneripetizioni.ui.Admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.service.InsegnanteService
import com.example.gestioneripetizioni.service.FeedbackService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {
    private lateinit var recyclerViewInsegnanti: RecyclerView
    private lateinit var recyclerViewFeedback: RecyclerView
    private lateinit var adapterInsegnanti: AdminInsegnantiAdapter
    private lateinit var adapterFeedback: AdminFeedbackAdapter

    private val insegnantiListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val insegnanti = snapshot.children.mapNotNull { it.getValue(com.example.gestioneripetizioni.model.Insegnante::class.java) }
            adapterInsegnanti.aggiornaInsegnanti(insegnanti)
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(this@AdminActivity, "Errore nel caricare gli insegnanti: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val feedbackListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val feedbacks = snapshot.children.mapNotNull { it.getValue(com.example.gestioneripetizioni.model.Feedback::class.java) }
            adapterFeedback.aggiornaFeedback(feedbacks)
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(this@AdminActivity, "Errore nel caricare i feedback: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        recyclerViewInsegnanti = findViewById(R.id.recyclerViewInsegnanti)
        recyclerViewFeedback = findViewById(R.id.recyclerViewFeedback)

        adapterInsegnanti = AdminInsegnantiAdapter { insegnante ->
            mostraDialogEliminaInsegnante(insegnante)
        }

        adapterFeedback = AdminFeedbackAdapter { feedback ->
            mostraDialogEliminaFeedback(feedback)
        }

        recyclerViewInsegnanti.layoutManager = LinearLayoutManager(this)
        recyclerViewInsegnanti.adapter = adapterInsegnanti

        recyclerViewFeedback.layoutManager = LinearLayoutManager(this)
        recyclerViewFeedback.adapter = adapterFeedback

        InsegnanteService.getInsegnantiRef().addValueEventListener(insegnantiListener)
        FeedbackService.getFeedbacksRef().addValueEventListener(feedbackListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        InsegnanteService.getInsegnantiRef().removeEventListener(insegnantiListener)
        FeedbackService.getFeedbacksRef().removeEventListener(feedbackListener)
    }

    override fun onResume() {
        super.onResume()
        aggiornaListe()
    }

    private fun aggiornaListe() {
        InsegnanteService.cercaInsegnanti(null, null) { insegnanti ->
            adapterInsegnanti.aggiornaInsegnanti(insegnanti)
        }
        FeedbackService.getFeedbacks { feedbacks ->
            adapterFeedback.aggiornaFeedback(feedbacks)
        }
    }

    private fun mostraDialogEliminaInsegnante(insegnante: com.example.gestioneripetizioni.model.Insegnante) {
        AlertDialog.Builder(this)
            .setTitle("Elimina Insegnante")
            .setMessage("Sei sicuro di voler eliminare ${insegnante.nome} ${insegnante.cognome}?")
            .setPositiveButton("Elimina") { _, _ ->
                InsegnanteService.eliminaInsegnante(insegnante.id,
                    onSuccess = {
                        aggiornaListe()
                        Toast.makeText(this, "Insegnante eliminato", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, "Errore: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun mostraDialogEliminaFeedback(feedback: com.example.gestioneripetizioni.model.Feedback) {
        val feedbackId = feedback.id ?: run {
            Toast.makeText(this, "ID feedback non disponibile.", Toast.LENGTH_SHORT).show()
            return
        }
        val feedbackTesto = feedback.testo ?: "[Testo non disponibile]"

        AlertDialog.Builder(this)
            .setTitle("Elimina Feedback")
            .setMessage("Sei sicuro di voler eliminare questo feedback?\n\n\"${feedbackTesto}\"")
            .setPositiveButton("Elimina") { _, _ ->
                FeedbackService.eliminaFeedback(feedbackId,
                    onSuccess = {
                        aggiornaListe()
                        Toast.makeText(this, "Feedback eliminato", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, "Errore: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Annulla", null)
            .show()
    }


}