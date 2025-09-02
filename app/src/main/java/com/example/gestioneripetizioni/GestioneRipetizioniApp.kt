package com.example.gestioneripetizioni

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class GestioneRipetizioniApp : Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

}