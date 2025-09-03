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
    //'lateinit' indica che verranno inizializzate in un secondo momento
    private lateinit var recyclerViewInsegnanti: RecyclerView
    private lateinit var recyclerViewFeedback: RecyclerView
    private lateinit var adapterInsegnanti: AdminInsegnantiAdapter
    private lateinit var adapterFeedback: AdminFeedbackAdapter

    private val insegnantiListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            //converte i dati ricevuti in una lista di oggetti Insegnante e aggiorna l'adapter
            val insegnanti = snapshot.children.mapNotNull { it.getValue(com.example.gestioneripetizioni.model.Insegnante::class.java) }
            adapterInsegnanti.aggiornaInsegnanti(insegnanti)
        }

        //gestione di eventuali errori durante il caricamento dei dati
        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(this@AdminActivity, "Errore nel caricare gli insegnanti: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val feedbackListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            //converte i dati ricevuti in una lista di oggetti Feedback e aggiorna l'adapter
            val feedbacks = snapshot.children.mapNotNull { it.getValue(com.example.gestioneripetizioni.model.Feedback::class.java) }
            adapterFeedback.aggiornaFeedback(feedbacks)
        }

        //gestione di eventuali errori nel caricamento dei dati
        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(this@AdminActivity, "Errore nel caricare i feedback: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    //metodo che viene chiamato alla creazione dell'activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        //inizializzazione delle recyclerView collegandole ai corrispettivi ID nel layout
        recyclerViewInsegnanti = findViewById(R.id.recyclerViewInsegnanti)
        recyclerViewFeedback = findViewById(R.id.recyclerViewFeedback)

        //inizializzazione dell'adapter per gli insegnanti con la gestione dell'eliminazione
        adapterInsegnanti = AdminInsegnantiAdapter { insegnante ->
            mostraDialogEliminaInsegnante(insegnante)
        }

        //inizializzazione dell'adapter per i feedback con la gestione dell'eliminazione
        adapterFeedback = AdminFeedbackAdapter { feedback ->
            mostraDialogEliminaFeedback(feedback)
        }

        //impostazione del layout di visualizzazione degli elementi
        recyclerViewInsegnanti.layoutManager = LinearLayoutManager(this)
        recyclerViewInsegnanti.adapter = adapterInsegnanti

        recyclerViewFeedback.layoutManager = LinearLayoutManager(this)
        recyclerViewFeedback.adapter = adapterFeedback

        //sincronizzazione con il database
        InsegnanteService.getInsegnantiRef().addValueEventListener(insegnantiListener)
        FeedbackService.getFeedbacksRef().addValueEventListener(feedbackListener)
    }

    //metodo utilizzato per chiudere l'attività
    override fun onDestroy() {
        super.onDestroy()
        InsegnanteService.getInsegnantiRef().removeEventListener(insegnantiListener)
        FeedbackService.getFeedbacksRef().removeEventListener(feedbackListener)
    }

    //metodo chiamato quando riparte l'activity che comprende l'aggiornamento delle liste
    override fun onResume() {
        super.onResume()
        aggiornaListe()
    }

    //metodo che permette di aggiornare le liste con i nuovi dati di insegnanti e feedback
    private fun aggiornaListe() {
        InsegnanteService.cercaInsegnanti(null, null) { insegnanti ->
            adapterInsegnanti.aggiornaInsegnanti(insegnanti)
        }
        FeedbackService.getFeedbacks { feedbacks ->
            adapterFeedback.aggiornaFeedback(feedbacks)
        }
    }

    //metodo per l'eliminazione dell'insegnante
    private fun mostraDialogEliminaInsegnante(insegnante: com.example.gestioneripetizioni.model.Insegnante) {
        AlertDialog.Builder(this)
            .setTitle("Elimina Insegnante")
            .setMessage("Sei sicuro di voler eliminare ${insegnante.nome} ${insegnante.cognome}?")
            .setPositiveButton("Elimina") { _, _ ->
                //servizio di eliminazione effettivo dell'insegnante
                InsegnanteService.eliminaInsegnante(insegnante.id,
                    onSuccess = {
                        //aggiornamento della lista dopo l'eliminazione
                        aggiornaListe()
                        Toast.makeText(this, "Insegnante eliminato", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        //gestione di eventuale errore nell'eliminazione dell'insegnante
                        Toast.makeText(this, "Errore: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    //metodo per l'eliminazione di un feedback
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
                //servizio di eliminazione effettivo del feedback
                FeedbackService.eliminaFeedback(feedbackId,
                    onSuccess = {
                        //aggiornamento della lista dopo l'eliminazione
                        aggiornaListe()
                        Toast.makeText(this, "Feedback eliminato", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        //gestione di eventuale errore nell'eliminazione del feedback
                        Toast.makeText(this, "Errore: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Annulla", null)
            .show()
    }


}