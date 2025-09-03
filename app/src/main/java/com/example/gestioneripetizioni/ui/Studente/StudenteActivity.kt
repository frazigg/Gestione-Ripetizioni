package com.example.gestioneripetizioni.ui.Studente

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.model.Insegnante
import com.example.gestioneripetizioni.service.InsegnanteService
import com.example.gestioneripetizioni.service.FeedbackService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@SuppressLint("Registered")
class StudenteActivity : AppCompatActivity() {
    //'lateinit' indica che verranno inizializzate in un secondo momento
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InsegnantiAdapter
    private lateinit var etMateria: EditText
    private lateinit var etOrario: EditText
    private lateinit var btnCerca: Button

    private var insegnantiListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Associa il file di layout XML a questa Activity
        setContentView(R.layout.activity_studente)

        // Collega le variabili ai componenti nel layout XML tramite il loro ID
        recyclerView = findViewById(R.id.recyclerViewInsegnanti)
        etMateria = findViewById(R.id.etMateria)
        etOrario = findViewById(R.id.etOrario)
        btnCerca = findViewById(R.id.btnCerca)

        //inizializzazione dell'adapter con le funzioni da eseguire al click dei pulsanti
        adapter = InsegnantiAdapter(
            onEmailClick = { insegnante ->
                inviaEmail(insegnante)
            },
            onCallClick = { insegnante ->
                chiamaTelefono(insegnante)
            },
            onFeedbackClick = { insegnante ->
                mostraDialogFeedback(insegnante)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Azione da eseguire al click del bottone "cerca Insegnanti"
        btnCerca.setOnClickListener {
            cercaInsegnanti()
        }

        cercaInsegnanti()
    }

    //metodo per la ricerca degli insegnanti
    private fun cercaInsegnanti() {
        //rimuove eventuali listener precedenti
        insegnantiListener?.let { InsegnanteService.getInsegnantiRef().removeEventListener(it) }

        //prende i testi dei campi rimuovendo eventuali spazi
        val materia = etMateria.text.toString().trim()
        val orario = etOrario.text.toString().trim()

        //salvataggio del listener
        insegnantiListener =
            InsegnanteService.getInsegnantiRef().addValueEventListener(object : ValueEventListener {
                //aggiornamento dati dal database
                override fun onDataChange(snapshot: DataSnapshot) {
                    //creazione lista di insegnanti con i dati ricevuti verificando i campi materia e orario
                    val risultati =
                        snapshot.children.mapNotNull { it.getValue(Insegnante::class.java) }
                            .filter { insegnante ->
                                val matchesMateria = materia.isBlank() || insegnante.materie.any {
                                    it.equals(
                                        materia,
                                        ignoreCase = true
                                    )
                                }
                                val matchesOrario = orario.isBlank() || insegnante.orari.any {
                                    it.equals(
                                        orario,
                                        ignoreCase = true
                                    )
                                }
                                matchesMateria && matchesOrario
                            }
                    //aggiornamento della lista dopo i controlli
                    adapter.aggiornaInsegnanti(risultati)
                }

                //gestione di eventuali errori con il database
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudenteActivity,
                        "Errore nel caricare gli insegnanti: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    //metodo chiamato quando l'activity termina
    override fun onDestroy() {
        super.onDestroy()
        insegnantiListener?.let { InsegnanteService.getInsegnantiRef().removeEventListener(it) }
    }
    //metodo per il collegamento con l'app predefinita dell'utente per l'invio della mail
    private fun inviaEmail(insegnante: Insegnante) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            //impostazione dell'indirizzo dell'insegnante come destinatario della mail
            data = Uri.parse("mailto:${insegnante.email}")
            //impostazione oggetto della mail
            putExtra(Intent.EXTRA_SUBJECT, "Richiesta ripetizioni")
            //impostazione messaggio della mail
            putExtra(
                Intent.EXTRA_TEXT,
                "Salve ${insegnante.nome}, sono interessato alle sue ripetizioni."
            )
        }
        startActivity(intent)
    }

    //metodo per permettere all'utente di chiamare il numero dell'insegnante
    private fun chiamaTelefono(insegnante: Insegnante) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            //impostazione del numero dell'insegnante come numero da chiamare
            data = Uri.parse("tel:${insegnante.telefono}")
        }
        startActivity(intent)
    }

    //metodo per permettere la creazione di un feedback
    private fun mostraDialogFeedback(insegnante: Insegnante) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
        val etFeedback = dialogView.findViewById<EditText>(R.id.etFeedback)

        //permette di scrivere un feedback e inviarlo
        AlertDialog.Builder(this)
            .setTitle("Lascia feedback per ${insegnante.nome} ${insegnante.cognome}")
            .setView(dialogView)
            .setPositiveButton("Invia") { _, _ ->
                val testo = etFeedback.text.toString()
                val insegnanteId = insegnante.id
                if (testo.isNotBlank()) {
                    //servizio per l'aggiunta del feedback
                    FeedbackService.aggiungiFeedback(
                        insegnanteId, testo, "Studente anonimo",
                        onSuccess = {
                            Toast.makeText(this, "Feedback inviato!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}
