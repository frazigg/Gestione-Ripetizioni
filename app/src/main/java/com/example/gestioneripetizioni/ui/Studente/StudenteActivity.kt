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
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InsegnantiAdapter
    private lateinit var etMateria: EditText
    private lateinit var etOrario: EditText
    private lateinit var btnCerca: Button

    private var insegnantiListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studente)

        recyclerView = findViewById(R.id.recyclerViewInsegnanti)
        etMateria = findViewById(R.id.etMateria)
        etOrario = findViewById(R.id.etOrario)
        btnCerca = findViewById(R.id.btnCerca)

        adapter = InsegnantiAdapter { insegnante ->
            mostraOpzioniContatto(insegnante)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnCerca.setOnClickListener {
            cercaInsegnanti()
        }

        cercaInsegnanti()
    }

    private fun cercaInsegnanti() {
        insegnantiListener?.let { InsegnanteService.getInsegnantiRef().removeEventListener(it) }

        val materia = etMateria.text.toString().trim()
        val orario = etOrario.text.toString().trim()

        insegnantiListener =
            InsegnanteService.getInsegnantiRef().addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
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
                    adapter.aggiornaInsegnanti(risultati)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@StudenteActivity,
                        "Errore nel caricare gli insegnanti: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        insegnantiListener?.let { InsegnanteService.getInsegnantiRef().removeEventListener(it) }
    }

    private fun mostraOpzioniContatto(insegnante: Insegnante) {
        val options = arrayOf("Invia Email, Chiama, Lascia Feedback")

        AlertDialog.Builder(this)
            .setTitle("Contatta ${insegnante.nome} ${insegnante.cognome}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> inviaEmail(insegnante)
                    1 -> chiamaTelefono(insegnante)
                    2 -> mostraDialogFeedback(insegnante)
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun inviaEmail(insegnante: Insegnante) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${insegnante.email}")
            putExtra(Intent.EXTRA_SUBJECT, "Richiesta rièetizioni")
            putExtra(
                Intent.EXTRA_TEXT,
                "Salve ${insegnante.nome}, sono interessato alle sue ripetizioni."
            )
        }
        startActivity(intent)
    }

    private fun chiamaTelefono(insegnante: Insegnante) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${insegnante.telefono}")
        }
        startActivity(intent)
    }

    private fun mostraDialogFeedback(insegnante: Insegnante) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
        val etFeedback = dialogView.findViewById<EditText>(R.id.etFeedback)

        AlertDialog.Builder(this)
            .setTitle("Lascia feedback per ${insegnante.nome} ${insegnante.cognome}")
            .setView(dialogView)
            .setPositiveButton("Invia") { _, _ ->
                val testo = etFeedback.text.toString()
                val insegnanteId = insegnante.id ?: run {
                    Toast.makeText(this, "ID insegnante non disponibile per il feedback.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (testo.isNotBlank()) {
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
