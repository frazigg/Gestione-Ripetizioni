package com.example.gestioneripetizioni.ui.Insegnante

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.model.Insegnante
import com.example.gestioneripetizioni.service.InsegnanteService


class InsegnanteProfileActivity : AppCompatActivity() {

    //'lateinit' indica che verranno inizializzate in un secondo momento
    private lateinit var tvNome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var etNome: EditText
    private lateinit var etCognome: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etMaterie: EditText
    private lateinit var etOrari: EditText
    private lateinit var btnSalva: Button
    private lateinit var btnLogout: Button
    private lateinit var rvFeedback: RecyclerView
    private lateinit var feedbackAdapter: InsegnanteFeedbackAdapter

    // Handler per eseguire operazioni sul thread principale
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Associa il file di layout XML a questa Activity
        setContentView(R.layout.activity_insegnante_profile)

        initViews()

        // Carica i dati dell'insegnante
        loadDataGradually()
    }

    private fun loadDataGradually() {
        mainHandler.postDelayed({

            //Verifica che l'activity sia ancora attiva
            if (!isFinishing && !isDestroyed) {
                loadInsegnanteData()
            }
        }, 100)
    }

    private fun initViews() {

        // Collega le variabili ai componenti nel layout XML tramite il loro ID
        tvNome = findViewById(R.id.tvNome) ?: throw IllegalStateException("tvNome non trovato")
        tvEmail = findViewById(R.id.tvEmail)
            ?: throw java.lang.IllegalStateException("tvEmail non trovato")
        etNome = findViewById(R.id.etNome) ?: throw IllegalStateException("etNome non trovato")
        etCognome =
            findViewById(R.id.etCognome) ?: throw IllegalStateException("etCognome non trovato")
        etTelefono =
            findViewById(R.id.etTelefono) ?: throw IllegalStateException("etTelefono non trovato")
        etMaterie =
            findViewById(R.id.etMaterie) ?: throw IllegalStateException("etMaterie non trovato")
        etOrari = findViewById(R.id.etOrari) ?: throw IllegalStateException("etOrari non trovato")
        btnSalva =
            findViewById(R.id.btnSalva) ?: throw IllegalStateException("btnSalva non trovato")
        btnLogout =
            findViewById(R.id.btnLogout) ?: throw IllegalStateException("btnLogout non trovato")
        rvFeedback =
            findViewById(R.id.rvFeedback) ?: throw IllegalStateException("rvFeedback non trovato")

        rvFeedback.layoutManager = LinearLayoutManager(this)

        //Inizializza l'adapter con una lista vuota e lo collega alla RecyclerView
        feedbackAdapter = InsegnanteFeedbackAdapter(emptyList())
        rvFeedback.adapter = feedbackAdapter

        // Azione da eseguire al click del bottone "Salva"
        btnSalva.setOnClickListener {
            salvaModifiche()
        }

        // Azione da eseguire al click del bottone "Logout"
        btnLogout.setOnClickListener {
            logout()
        }

    }

    //Popola i campi della UI con i dati dell'oggetto Insegnante
    private fun loadBasicData(insegnante: Insegnante){
            tvNome.text = "${insegnante.getSafeNome()} ${insegnante.getSafeCognome()}"
            tvEmail.text = insegnante.getSafeEmail()
            etNome.setText(insegnante.getSafeNome())
            etCognome.setText(insegnante.getSafeCognome())
            etTelefono.setText(insegnante.getSafeTelefono())
            etMaterie.setText(insegnante.getSafeMaterie().joinToString(", "))
            etOrari.setText(insegnante.getSafeOrari().joinToString(", "))
    }

    //Carica in modo sicuro i feedback
    private fun loadFeedbackSafely(userId: String) {
        if (!::feedbackAdapter.isInitialized) return

        //Richiede i feedback al service
        InsegnanteService.getFeedbackForInsegnante(userId) { feedbackList ->

            //Esegue l'aggiornamento della UI sul thread principale
            mainHandler.post {
                if (isFinishing || isDestroyed) return@post

                val safeFeedbackList =

                    //Filtra la lista per rimuovere feedback nulli o non validi
                    feedbackList?.filter { it != null && it.isValid() } ?: emptyList()

                //Aggiorna con la nuova lista
                feedbackAdapter.aggiornaFeedback(safeFeedbackList)
            }
        }
    }

    private fun loadInsegnanteData() {
        InsegnanteService.getCurrentInsegnante { insegnante ->
            if (isFinishing || isDestroyed) return@getCurrentInsegnante

            //Se l'insegnante esiste ed è valido, popola la UI
            if (insegnante != null && insegnante.isValid()) {
                loadBasicData(insegnante)
                loadFeedbackSafely(insegnante.id)

            } else {
                //Mostra un errore e fai il logout
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(
                        this,"Impossibile caricare i dati dell'utente. Effettuare nuovamente il login.", Toast.LENGTH_LONG).show()
                    logout()
                }
            }
        }
    }

    private fun salvaModifiche(){
        val nome = etNome.text.toString().trim()
        val cognome = etCognome.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val materie = etMaterie.text.toString().trim()
        val orari = etOrari.text.toString().trim()

        //Controlla che ogni campo contenga informazioni
        if(nome.isEmpty()||cognome.isEmpty()||telefono.isEmpty()||materie.isEmpty()||orari.isEmpty()){
            Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_LONG).show()
            return
        }

        //Converte le stringhe di materie e orari in liste
        val materieList = materie.split(",").map { it.trim() }
        val orariList = orari.split(",").map { it.trim() }

        //Chiama il service per aggiornare il profilo
        InsegnanteService.updateInsegnanteProfile(nome, cognome, telefono, materieList, orariList,
            onSuccess = {
                Toast.makeText(this, "Profilo aggiornato con successo!", Toast.LENGTH_SHORT).show()
                loadInsegnanteData()
            },
            onFailure = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )
    }
    private fun logout(){
        InsegnanteService.logoutInsegnante()
        val intent = Intent(this, InsegnanteLoginActivity::class.java)

        //Queste flag puliscono la cronologia delle activity, impedendo all'utente di tornare alla schermata del profilo con il tasto "indietro".
        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}