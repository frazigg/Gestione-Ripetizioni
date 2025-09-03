
package com.example.gestioneripetizioni.service

import com.example.gestioneripetizioni.model.Feedback
import com.example.gestioneripetizioni.model.Insegnante
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

//creazione della classe InsegnanteService per la gestione degli insegnanti
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

        //Recupera l'utente da FireBase Authentication
        val firebaseUser = auth.currentUser
        if (firebaseUser != null){

            //Accedi al nodo specifico dell'insegnante attraverso l'uid e legge i dati, infine restituisce l'insegnante
            insegnantiRef.child(firebaseUser.uid).addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot){
                        val insegnante = snapshot.getValue(Insegnante::class.java)
                        currentInsegnante = insegnante
                        onResult(insegnante)
                    }

                    //Se l'operazione viene annullata restituisce null
                    override fun onCancelled(error: DatabaseError) {
                        onResult(null)
                    }
                })
        } else {
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
        onFailure: (String) -> Unit
    ) {

        //Recupera l'ID dell'utente corrente
        val userId = auth.currentUser?.uid
        if (userId != null) {

            //Mappa con i dati da aggiornare
            val updates = mapOf(
                "nome" to nome,
                "cognome" to cognome,
                "telefono" to telefono,
                "materie" to materie,
                "orari" to orari
            )

            //Aggiornamento dei dati sul nodo specifico dell'insegnante
            insegnantiRef.child(userId).updateChildren(updates)
                .addOnSuccessListener {

                    //Se l'aggiornamento va a buon fine, aggiorna l'oggetto locale
                    currentInsegnante?.apply {
                        this.nome = nome
                        this.cognome = cognome
                        this.telefono = telefono
                        this.materie = materie
                        this.orari = orari
                    }

                    //Callback di successo
                    onSuccess()
                }
                .addOnFailureListener { e ->

                    //Callback di errore
                    onFailure(e.message ?: "Errore durante l'aggiornamewnto del profilo.")
                }
        } else {
            onFailure("Utente non autenticato.")
        }
    }

    //Funzione per cercare gli insegnanti in base alla materia e orario
    fun cercaInsegnanti(
        materia: String?,
        orario: String?,
        onResult: (List<Insegnante>) -> Unit
    ) {

        //Legge tutti i dati dal nodo insegnanti
        insegnantiRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val risultati = mutableListOf<Insegnante>()
                for(childSnapshot in snapshot.children){
                    val insegnante = childSnapshot.getValue(Insegnante::class.java)
                    if(insegnante != null){

                        //Controlla se la materia corrisponde (o se il filtro è vuoto)
                        val matchesMateria = materia.isNullOrBlank()||insegnante.materie.any{it.equals(materia, ignoreCase = true) }

                        //Controlla se la materia corrisponde (o se il filtro è vuoto)
                        val matchesOrario = orario.isNullOrBlank()||insegnante.orari.any{it.equals(orario, ignoreCase = true) }

                        //Se rispetta i criteri aggiunge l'insegnante ai risultati disponibili
                        if(matchesMateria && matchesOrario){
                            risultati.add(insegnante)
                        }
                    }
                }
                onResult(risultati)
            }

            override fun onCancelled(error: DatabaseError){

                //In caso di errore restituisce una lista vuota
                onResult(emptyList())
            }
        })
    }

    //Funzione per l'eliminazione di un profilo insegnante
    fun eliminaInsegnante(
        id:String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        //Prova ad eliminare l'utente dal sistema di autenticazione
        auth.currentUser?.delete()
            ?.addOnCompleteListener { authTask ->

                //Se l'eliminazione ha successo
                if(authTask.isSuccessful){

                    //Procedi a rimuovere i dati dell'insegnante dal database
                    insegnantiRef.child(id).removeValue()
                        .addOnSuccessListener {

                            //Dopo la rimozione dell'insegnante, rimuovi anche i feedback associati
                            feedbackRef.orderByChild("insegnanteId").equalTo(id)
                                .addListenerForSingleValueEvent(object: ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children){
                                            childSnapshot.ref.removeValue()
                                        }

                                        //Callback di successo
                                        onSuccess()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        onFailure(error.message)
                                    }
                                })
                        }
                        .addOnFailureListener { e ->
                            onFailure(e.message ?: "Errore durante l'eliminazione dell'insegnante dal database.")
                        }
                } else {
                    onFailure(authTask.exception?.message ?: "Errore durante l'eliminazione dell'utente dall'autenticazione.")
                }
            }
    }

    //Funzione per ottenere i feedback relativi ad un insegnante
    fun getFeedbackForInsegnante(
        insegnanteId: String,
        onResult: (List<Feedback>) -> Unit
    ){
        //Se l'ID è vuoto, restituisci una lista vuota
        if(insegnanteId.isBlank()){
            onResult(emptyList())
            return
        }

        //Cerco nel nodo feedbacks tutti gli elementi che corrispondono all'ID fornito
        feedbackRef.orderByChild("insegnanteId").equalTo(insegnanteId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val feedbacks = mutableListOf<Feedback>()

                    for(childSnapshot in snapshot.children){
                        val feedback = childSnapshot.getValue(Feedback::class.java)

                        //Controlla la validità del feedback e che appartiene veramente a quell'insegnante
                        if(feedback != null && feedback.isValid() && feedback.insegnanteId == insegnanteId) {
                            feedbacks.add(feedback)
                        }
                    }

                    //Restituisce la lista dei feedback
                    onResult(feedbacks)
                }

                override fun onCancelled(error: DatabaseError) {

                    //In caso di errore, restituisce una lista vuota
                    onResult(emptyList())
                }
            })
    }
}