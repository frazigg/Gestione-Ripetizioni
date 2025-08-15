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
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistra: Button
    private lateinit var tvTitolo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insegnante_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistra = findViewById(R.id.btnRegistra)
        tvTitolo = findViewById(R.id.tvTitolo)

        tvTitolo.text = "accesso Insegnanti"

        btnLogin.setOnClickListener {
            loginInsegnante()
        }

        btnRegistra.setOnClickListener {
            val intent = Intent(this, InsegnanteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginInsegnante(){
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false

        lifecycleScope.launch {
            try{
                InsegnanteService.loginInsegnante(email,password)

                val intent = Intent(this@InsegnanteLoginActivity, InsegnanteProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception){
                val messaggio = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Credenziali non valide. Controlla email e password."
                    is IllegalStateException -> "Login fallito: ${e.message}"
                    else -> "Si è verificato un errore imprevisto: ${e.message}"
                }
                Toast.makeText(this@InsegnanteLoginActivity, messaggio, Toast.LENGTH_LONG).show()
            } finally {
                btnLogin.isEnabled = true
            }
        }
    }
}

