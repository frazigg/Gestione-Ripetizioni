package com.example.gestioneripetizioni.ui.Insegnante

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestioneripetizioni.R
import com.example.gestioneripetizioni.service.InsegnanteService
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

@SuppressLint("Registered")
class InsegnanteActivity : AppCompatActivity(){

    private lateinit var etNome: EditText
    private lateinit var etCognome: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etPassword: EditText
    private lateinit var etMaterie: EditText
    private lateinit var etOrari: EditText
    private lateinit var btnRegistra: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insegnante)

        etNome = findViewById(R.id.etNome)
        etCognome = findViewById(R.id.etCognome)
        etEmail = findViewById(R.id.etEmail)
        etTelefono = findViewById(R.id.etTelefono)
        etPassword = findViewById(R.id.etPassword)
        etMaterie = findViewById(R.id.etMaterie)
        etOrari = findViewById(R.id.etOrari)
        btnRegistra = findViewById(R.id.btnRegistra)

        btnRegistra.setOnClickListener {
            registraInsegnante()
        }
    }

    private fun registraInsegnante(){
        val nome = etNome.text.toString().trim()
        val cognome = etCognome.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val materie = etMaterie.text.toString().trim()
        val orari = etOrari.text.toString().trim()

        if(nome.isEmpty()||cognome.isEmpty()||email.isEmpty()||telefono.isEmpty()||password.isEmpty()||orari.isEmpty()){
            Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show()
            return
        }

        if(password.length < 6){
            Toast.makeText(this,"La password deve essere di almeno 6 caratteri", Toast.LENGTH_SHORT).show()
            return
        }

        btnRegistra.isEnabled = false

        lifecycleScope.launch{
            try{
                val materieList = materie.split(",").map{it.trim()}
                val orariList = orari.split(",").map{it.trim()}

                InsegnanteService.registraInsegnante(
                    nome, cognome, email, telefono, password, materieList, orariList
                )

                Toast.makeText(this@InsegnanteActivity, "Registrazione completata!", Toast.LENGTH_LONG).show()
                val intent = Intent(this@InsegnanteActivity, InsegnanteLoginActivity::class.java)
                startActivity(intent)
                finish()
            }catch (e: Exception){
                val messaggio = when (e){
                    is FirebaseAuthUserCollisionException -> "L'indirizzo email è già in uso da un altro account."
                    else -> e.message ?: "Si è verificato un errore imprevisto."
                }
                Toast.makeText(this@InsegnanteActivity, messaggio, Toast.LENGTH_LONG).show()
            } finally {
                btnRegistra.isEnabled = true
            }
        }
    }
}