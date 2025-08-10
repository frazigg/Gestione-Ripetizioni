package com.example.gestioneripetizioni.model

import com.example.gestioneripetizioni.model.Feedback

data class Insegnante (
    var id: String = "",
    var nome: String = "",
    var cognome: String = "",
    var email: String = "",
    var telefono: String = "",
    var password: String = "",
    var materie: List<String> = emptyList(),
    var orari: List<String> = emptyList()
) {
    //Verifica se l'insegnante è valido
    fun isValid(): Boolean{
        return !id.isBlank() &&
                !nome.isBlank() &&
                !cognome.isBlank() &&
                !email.isBlank()
    }

//Eseguiamo dei controlli sulle variabili di Insegnante

    //Controlliamo che il nome non sia vuoto
    fun getSafeNome(): String{
        return nome.trim().ifBlank { "Nome non valido" }
    }

    //Controlliamo che il cognome non sia vuoto
    fun getSafeCognome(): String{
        return cognome.trim().ifBlank { "Cognome non valido" }
    }

    //Controlliamo che la mail non sia vuota
    fun getSafeEmail(): String {
        return email.trim().ifBlank { "Email non valida" }
    }

    //Controlliamo che il numero di telefono sia valido
    fun getSafeTelefono(): String {
        return telefono.trim().ifBlank { "Numero non valido" }
    }

    //Controlliamo che la lista di materie non sia vuota
    fun getSafeMaterie(): List<String>{
        return materie.filter { it.isNotBlank() }.ifEmpty { listOf("Materie non specificate") }
    }

    //Controlliamo che la lista degli orari non sia vuota
    fun getSafeOrari(): List<String>{
        return orari.filter { it.isNotBlank() }.ifEmpty { listOf("Orari non specificati") }
    }
}