package com.example.gestioneripetizioni

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.gestioneripetizioni.ui.Studente.StudenteActivity
import com.example.gestioneripetizioni.ui.Insegnante.InsegnanteLoginActivity
import com.example.gestioneripetizioni.ui.Admin.AdminActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStudente).setOnClickListener {
            startActivity(Intent(this, StudenteActivity::class.java))
        }

        findViewById<Button>(R.id.btnInsegnante).setOnClickListener {
            startActivity(Intent(this, InsegnanteLoginActivity::class.java))
        }

        findViewById<Button>(R.id.btnAdmim).setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
    }
}