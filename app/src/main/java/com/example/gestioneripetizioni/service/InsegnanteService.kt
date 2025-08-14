package com.example.gestioneripetizioni.service

import com.example.gestioneripetizioni.model.Insegnante
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
            /* in caso di errore elimina l'insegnante dal database e solleva l'eccezione affinché venga gestita*/
            auth.currentUser?.delete()?.await()
            throw e
        }
    }

    suspend fun loginInsegnante(email: String, password: String): Insegnante{
        try{
            //Autentica l'utente con Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email,password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Login fallito: utente non trovato.")

            //Scarica i dati dell'insegnante dal databse
            val dataSnapshot = insegnantiRef.child(firebaseUser.uid).get().await()
            val insegnante = dataSnapshot.getValue(Insegnante::class.java)?: throw IllegalStateException("Dati dell'insegnante " +
                    "non trovati nel databse. La deserializzazione potrebbe essere fallita. ")

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




}