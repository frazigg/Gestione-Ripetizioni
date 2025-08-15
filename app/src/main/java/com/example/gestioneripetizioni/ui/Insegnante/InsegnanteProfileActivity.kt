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

    private lateinit var tvNome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var etNome: EditText
    private lateinit var etCognome: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etMateria: EditText
    private lateinit var etOrari: EditText
    private lateinit var btnSalva: Button
    private lateinit var btnLogout: Button
    private lateinit var rvFeedback: RecyclerView
    private lateinit var feedbackAdapter: InsegnanteFeedbackAdapter
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCReate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insegnante_profile)

        try{
            initViews()
            loadDataGrandually()
        }
    }

    private fun loadDataGradually(){
        mainHandler.postDelayed({
            if(!isFinishing && !isDestroyed){
                loadInsegnanteData()
            }
        }, 100)
    }

    private fun initViews(){
        try{
            tvNome = findViewById(R.id.tvNome) ?: throw IllegalStateException("tvNome non trovato")
            tvEmail= findViewById(R.id.tvEmail) ?: throw java.lang.IllegalStateException("tvEmail non trovato")
            etNome = findViewById(R.id.etNome) ?: throw IllegalStateException("etNome non trovato")
            etCognome = findViewById(R.id.etCognome) ?: throw IllegalStateException("etCognome non trovato")
            etTelefono = findViewById(R.id.etTelefono) ?: throw IllegalStateException("etTelefono non trovato")
            etMaterie = findViewById(R.id.etMaterie) ?: throw IllegalStateException("etMaterie non trovato")
            etOrari = findViewById(R.id.etOrari) ?: throw IllegalStateException("etOrari non trovato")
            btnSalva = findViewById(R.id.btnSalva) ?: throw IllegalStateException("btnSalva non trovato")
            btnLogout = findViewById(R.id.btnLogout) ?: throw IllegalStateException("btnLogout non trovato")
            rvFeedback = findViewById(R.id.rvFeedback) ?: throw IllegalStateException("rvFeedback non trovato")

            rvFeedback.layoutManager = LinearLayoutManager(this)
            feedbackAdapter = InsegnanteFeedbackAdapter(emptyList())
            rvFeedback.adapter = feedbackAdapter
            btnSalva.setOnClickListener{
                salvaModifiche()
            }
            btnLogout.setOnClickListener {
                logout()
            }
            println("DEBUG: Tutti i view inizializzati correttamente")
        }catch(e: Exception){
            println("DEBUG: Errore nell'inizializzazione dei view: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun loadInsegnanteData(){
        println("DEBUG: Iniziando caricamento dati insegnante")
        try{
            InsegnanteService.getCurrentInsegnante{ insegnante ->
                if(isFinishing||isDestroyed) return@getCurrentInsegnante

                if(insegnante != null && insegnante.isValid()){
                    try {
                        localBasicData(insegnante)
                        localFeedbackSafely(insegnante.id)
                    }catch (e: Exception){
                        println("DEBUG: Errore nell'impostazione dati UI: ${e.message}")
                        if(!isFinishing&&!isDestroyed){
                            Toast.makeText(this, "Errore nel caricamento dei dati", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    if(!isFinishing&&!isDestroyed){
                        Toast.makeText(this, "Impossibile caricare i dati dell'utente. Effettuare nuovamente il login.", Toast.LENGTH_LONG).show()
                        logout()
                    }
                }

            }
        }catch (e: Exception){
            println("DEBUG: Errore nell'impostazione dati UI: ${e.message}")
            if(!isFinishing&&!isDestroyed){
                Toast.makeText(this, "Errore critico nel caricamento dei dati", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


}