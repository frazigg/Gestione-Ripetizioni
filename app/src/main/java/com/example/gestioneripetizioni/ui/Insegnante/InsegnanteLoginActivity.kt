package com.example.gestioneripetizioni.ui.Insegnante

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.service.InsegnanteService
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.launch

@SuppressLint("Registered")
public class InsegnanteLoginActivity : AppCompatActivity() {
    //'lateinit' indica che verranno inizializzate in un secondo momento
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistra: Button
    private lateinit var tvTitolo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Associa il file di layout XML a questa Activity
        setContentView(R.layout.activity_insegnante_login)

        // Collega le variabili ai componenti nel layout XML tramite il loro ID
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistra = findViewById(R.id.btnRegistra)
        tvTitolo = findViewById(R.id.tvTitolo)

        tvTitolo.text = "accesso Insegnanti"

        // Azione da eseguire al click del bottone "Login"
        btnLogin.setOnClickListener {
            loginInsegnante()
        }

        // Azione da eseguire al click del bottone "Registra"
        btnRegistra.setOnClickListener {
            val intent = Intent(this, InsegnanteActivity::class.java)
            startActivity(intent)
        }
    }

    //Recupera il testo dei campi email e password eliminando eventuali spazi
    private fun loginInsegnante(){
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        //Verifica che i campi email e password non siano vuoti ed eventualmente manda un messaggio di avviso
        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show()
            return
        }
        //Disabilita il bottone "Login"
        btnLogin.isEnabled = false

        //Esegue il login
        lifecycleScope.launch {
            try{
                InsegnanteService.loginInsegnante(email,password)

                //Avvia InsegnanteProfileActivity se il login viene effettuato nel modo corretto
                val intent = Intent(this@InsegnanteLoginActivity, InsegnanteProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception){
                //Gestisce eventuali errori nel caso in cui il login dovesse fallire e invia un messaggio di avviso
                val messaggio = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Credenziali non valide. Controlla email e password."
                    is IllegalStateException -> "Login fallito: ${e.message}"
                    else -> "Si è verificato un errore imprevisto: ${e.message}"
                }
                Toast.makeText(this@InsegnanteLoginActivity, messaggio, Toast.LENGTH_LONG).show()
            } finally {
                //Riabilita il bottone "Login"
                btnLogin.isEnabled = true
            }
        }
    }
}

