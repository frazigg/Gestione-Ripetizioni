@file:Suppress("ReplacePrintlnWithLogging")

package com.example.gestioneripetizioni.service

import com.example.gestioneripetizioni.model.Feedback
import com.example.gestioneripetizioni.model.Insegnante
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

/*creazione della classe InsegnanteService per la gestione degli insegnanti */
object InsegnanteService {
    init {
        /* disabilita la modalità offline del Firebase */
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
    }

    /* viene creato il riferimento al servizio di autenticazione del database */
    private val auth = FirebaseAuth.getInstance()

    /* collegamento con il database */
    private val database =
        FirebaseDatabase.getInstance("https://progettomobili-e5b92-default-rtdb.europe-west1.firebasedatabase.app/")

    /* creazione di un riferimento a insegnanti all'interno del database */
    private val insegnantiRef = database.getReference("insegnanti")

    /* creazione di un riferimento a feedbacks all'interno del database */
    private val feedbackRef = database.getReference("feedbacks")

    /*metodo che restituisce il riferimento a insegnanti */
    fun getInsegnantiRef() = insegnantiRef

    /* variabile che memorizza l'insegnante loggato o appena registrato */
    private var currentInsegnante: Insegnante? = null

    /* creazione del metodo registraInsegnante per la registrazione di un nuovo insegnante */
    suspend fun registraInsegnante(
        nome: String,
        cognome: String,
        email: String,
        telefono: String,
        password: String,
        materie: List<String>,
        orari: List<String>
    ): Insegnante {
        try {
            /* viene creato un nuovo account con l'email e la password nel Firebase */
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            /* viene creato il riferimento all'utente appena creato nel Firebase */
            val firebaseUser =
                authResult.user ?: throw IllegalStateException("Creazione utente fallita")

            /* crea un oggetto di tipo Insegnante con i dati inseriti alla registraizone */
            val insegnante = Insegnante(
                id = firebaseUser.uid,
                nome = nome,
                cognome = cognome,
                email = email,
                telefono = telefono,
                password = password,
                materie = materie,
                orari = orari
            )

            /* salva l'insegnante registrato nel database */
            insegnantiRef.child(firebaseUser.uid).setValue(insegnante).await()

            /* aggiorna il riferimento all'insegnante corrente con quello appena registrato */
            currentInsegnante = insegnante
            /* restituisce l'insegnante appena registrato */
            return insegnante
        } catch (e: Exception) {
            /* in caso di errore elimina l'insegnante dal database e solleva l'eccezione affinché
            venga gestita*/
            auth.currentUser?.delete()?.await()
            throw e
        }
    }

    suspend fun loginInsegnante(email: String, password: String): Insegnante{
        try{
            //Autentica l'utente con Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email,password).await()
            val firebaseUser =
                authResult.user ?: throw IllegalStateException("Login fallito: utente non trovato.")

            //Scarica i dati dell'insegnante dal databse
            val dataSnapshot = insegnantiRef.child(firebaseUser.uid).get().await()
            val insegnante =
                dataSnapshot.getValue(Insegnante::class.java) ?: throw IllegalStateException(
                    "Dati dell'insegnante non trovati nel databse. La deserializzazione" +
                            "potrebbe essere fallita. "
                )

            //Imposta l'utente
            currentInsegnante = insegnante

            return insegnante
        }catch (e : Exception) {
            throw e
        }
    }

    //Funzione per il logout dell'insegnante
    fun logoutInsegnante() {
        auth.signOut()
        currentInsegnante = null
    }

    //Funzione per determinare qual è l'insegnante corrente
    fun getCurrentInsegnante(onResult: (Insegnante?) -> Unit){
        try{
            val firebaseUser = auth.currentUser
            if (firebaseUser != null){
                println("DEBUG: getCurrentInsegnante - Utente Firebase trovato: ${firebaseUser.uid}")
                insegnantiRef.child(firebaseUser.uid).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot){
                            try{
                                println("DEBUG: getCurrentInsegnante - Dati ricevuti da Firebase")
                                val insegnante = snapshot.getValue(Insegnante::class.java)
                                if (insegnante != null){
                                    println("DEBUG: getCurrentInsegnante - Insegnante deserializzato: ${insegnante.id}")
                                    currentInsegnante = insegnante
                                    onResult(insegnante)
                                } else {
                                    println("DEBUG: getCurrentInsegnante - Insegnante null dopo deserializzazione")
                                    onResult(null)
                                }
                            } catch (e: Exception){
                                println("DEBUG: getCurrentInsegnante - Errore nella deserializzazione: ${e.message}")
                                e.printStackTrace()
                                onResult(null)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            println("DEBUG: getCurrentIngegnante - Errore Firebase: ${error.message}")
                            onResult(null)
                        }
                    })
            } else {
                println("DEBUG: getCurrentInsegnante - Nessun utente Firebase autenticato")
                onResult(null)
            }
        } catch (e: Exception){
            println("DEBUG: getCurrentInsegnante - Errore generale: ${e.message}")
            e.printStackTrace()
            onResult(null)
        }
    }

    //Funzione per aggiornati i dati del profilo di un insegnante già registrato
    fun updateInsegnanteProfile(
        nome: String,
        cognome: String,
        telefono: String,
        materie: List<String>,
        orari: List<String>,
        onSuccess: () -> Unit,
        onFailur: (String) -> Unit
    ) {
        //
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val updates = mapOf(
                "nome" to nome,
                "cognome" to cognome,
                "telefono" to telefono,
                "materie" to materie,
                "orari" to orari
            )
            insegnantiRef.child(userId).updateChildren(updates)
                .addOnSuccessListener {
                    currentInsegnante?.apply {
                        this.nome = nome
                        this.cognome = cognome
                        this.telefono = telefono
                        this.materie = materie
                        this.orari = orari
                    }
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailur(e.message ?: "Errore durante l'aggiornamewnto del profilo.")
                }
        } else {
            onFailur("Utente non autenticato.")
        }
    }

    //Funzione per cercare gli insegnanti in base alla materia e orario
    fun cercaInsegnanti(
        materia: String?,
        orario: String?,
        onResult: (List<Insegnante>) -> Unit
    ) {
        insegnantiRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val risultati = mutableListOf<Insegnante>()
                for(childSnapshot in snapshot.children){
                    val insegnante = childSnapshot.getValue(Insegnante::class.java)
                    if(insegnante != null){
                        val matchesMateria = materia.isNullOrBlank()||insegnante.materie.any{it.equals(materia, ignoreCase = true) }
                        val matchesOrario = orario.isNullOrBlank()||insegnante.orari.any{it.equals(orario, ignoreCase = true) }
                        if(matchesMateria && matchesOrario){
                            risultati.add(insegnante)
                        }
                    }
                }
                onResult(risultati)
            }

            override fun onCancelled(error: DatabaseError){
                onResult(emptyList())
            }
        })
    }

    //Funzione per l'eliminazione di un profilo insegnante
    fun eliminaInsegnante(
        id:String,
        onSuccess: () -> Unit,
        onFailur: (String) -> Unit
    ) {
        auth.currentUser?.delete()
            ?.addOnCompleteListener { authTask ->
                if(authTask.isSuccessful){
                    insegnantiRef.child(id).removeValue()
                        .addOnSuccessListener {
                            feedbackRef.orderByChild("insegnanteId").equalTo(id)
                                .addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children){
                                            childSnapshot.ref.removeValue()
                                        }
                                        onSuccess()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        onFailur(error.message)
                                    }
                                })
                        }
                        .addOnFailureListener { e ->
                            onFailur(e.message ?: "Errore durante l'eliminazione dell'insegnante dal database.")
                        }
                } else {
                    onFailur(authTask.exception?.message ?: "Errore durante l'eliminazione dell'utente dall'autenticazione.")
                }
            }
    }

    //Funzione per ottenere i feedback relativi ad un insegnante
    fun getFeedbackForInsegnante(
        insegnanteId: String,
        onResult: (List<Feedback>) -> Unit
    ){
        if(insegnanteId.isBlank()){
            onResult(emptyList())
            return
        }

        try{
            feedbackRef.orderByChild("insegnanteId").equalTo(insegnanteId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val feedbacks = mutableListOf<Feedback>()

                        for(childSnapshot in snapshot.children){
                            try{
                                val feedback = childSnapshot.getValue(Feedback::class.java)
                                if(feedback != null && feedback.isValid() && feedback.insegnanteId == insegnanteId) {
                                    feedbacks.add(feedback)
                                }
                            } catch (e: Exception){
                                println("DEBUG: Errore nella deserializzazione del feedback: ${e.message}")
                            }
                        }

                        onResult(feedbacks)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("DEBUG: Errore generale in getFeedbackForInsegnante: ${error.message}")
                        onResult(emptyList())
                    }
                })
        }catch (e: Exception){
            println("DEBUG: Errore generale in getFeedbackForInsegnante: ${e.message}")
            onResult(emptyList())
        }
    }
}