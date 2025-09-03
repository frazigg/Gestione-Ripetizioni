package com.example.gestioneripetizioni.service

import com.example.gestioneripetizioni.model.Feedback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/* creazione della classe FeedbackService per la gestione dei feedback */
object FeedbackService{

/* collegamento con il database */
    private val database = FirebaseDatabase.getInstance("https://progettomobili-e5b92-default-rtdb.europe-west1.firebasedatabase.app/")
/* creazione di un riferimento a feedbacks all'interno del database */
    private val feedbacksRef = database.getReference("feedbacks")
/*metodo che restituisce il riferimento a feedbacks */
    fun getFeedbacksRef() = feedbacksRef

/* creazione del metodo aggiungiFeedback per permettere la creazione di nuovi feedback */
    fun aggiungiFeedback(
        insegnanteId: String,
        testo: String,
        autore: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
    /* controllo che l'ID insegnante non sia vuoto */
        if(insegnanteId.isBlank()){
            onFailure("ID insegnante non valido")
            return
        }
    /* controllo che il testo del feedback non sia vuoto */
        if(testo.isBlank()){
            onFailure("Il testo non può essere vuoto")
            return
        }
    /* vengono eliminati gli spazi e viene sostituito l'autore con "Anonimo" */
        val feedbackId = feedbacksRef.push().key
        if (feedbackId != null){
            val feedback = Feedback(
                id = feedbackId,
                insegnanteId = insegnanteId,
                testo = testo.trim(),
                autore = autore.trim().ifBlank{ "Anonimo" }
            )
        /* il feedback viene salvato sul Firebase e viene inviato un messaggio in caso di errore */
            feedbacksRef.child(feedbackId).setValue(feedback)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure( e.message ?: "Errore durante l'aggiunta del feedback." ) }
        } else {
            onFailure( "Impossibile generare ID per il feedback." )
        }
    }
/* creazione metoto eliminaFeedback per rimuovere un feedback dal database */
    fun eliminaFeedback(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
    /* viene inviato un messaggio nel caso in cui la rimozione non abbia successo */
        feedbacksRef.child( id).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener{ e -> onFailure(e.message ?: "Errore durante l'eliminazione del feedback.") }
    }
/* creazione del metodo getFeedback per ottenere la lista di tutti i feedback nel database */

    fun getFeedbacks(onResult: (List<Feedback>) -> Unit){
        feedbacksRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot){

        /* viene creata una lista che conterrà i feedback validi presenti nel dabase */
            val feedbacks = mutableListOf<Feedback>()

            /* con il ciclo for si scorrono tutti i feedback e vengono creati degli oggetti di tipo Feedback */
                for(childSnapshot in snapshot.children){
                    try{
                        val feedback = childSnapshot.getValue(Feedback::class.java)

                   /* se gli oggetti creati non sono nulli e tutti i loro campi sono validi venogno aggiunti alla lista creata precedentemente */
                        if (feedback != null && feedback.isValid()){
                            feedbacks.add(feedback)
                        }

                /* se si verifica un errore con un feedback viene gestito con un messaggio e si passa al feedback successivo */
                    }catch (e: Exception){
                        println("Errore nella deserializzazione del feedback: ${e.message}")
                    }
                }

            /* restituisce la lista completa di feedback validi */
                onResult(feedbacks)
            }

        /* gestione  degli errori di lettura restituiendo una lista vuota se il caricamento del feedback fallisce */
            override fun onCancelled(error: DatabaseError) {
                println("Errore nel caricamento del feedback: ${error.message}")
                onResult(emptyList())
            }
        })
    }
}
