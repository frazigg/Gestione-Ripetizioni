package com.example.gestioneripetizioni.service

import com.example.gestioneripetizioni.model.Feedback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FeedbackService{
    init{
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
    }
    private val database = FirebaseDatabase.getInstance("https://progettomobili-e5b92-default-rtdb.europe-west1.firebasedatabase.app/")
    private val feedbacksRef = database.getReference("feedbacks")

    fun getFeedbacksRef() = feedbacksRef

    fun aggiungiFeedback(
        insegnanteId: String,
        testo: String,
        autore: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if(insegnanteId.isBlank()){
            onFailure("ID insegnante non valido")
            return
        }

        if(testo.isBlank()){
            onFailure("Il testo non può essere vuoto")
            return
        }



    }
}
