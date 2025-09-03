package com.example.gestioneripetizioni

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class GestioneRipetizioniApp : Application(){
    override fun onCreate() {
        super.onCreate()
        //permette il funzionamento offline del database grazie alla creazione di una copia locale
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

}