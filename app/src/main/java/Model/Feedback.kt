package Model

data class Feedback (
    var id: String? = null,
    var insegnanteId: String? = null,
    var testo: String? = null,
    var autore: String? = null
) {
    //Verifica se il Feedback è valido
    fun isValid(): Boolean{
        return !id.isNullOrBlank() &&
                !insegnanteId.isNullOrBlank() &&
                !testo.isNullOrBlank()
    }

    //Verifica che il testo non sia vuoto
    fun getSafeTesto(): String {
        return testo?.trim() ?: "[Testo non disponibile]"
    }

    //Verifica sull'autore
    fun getSafeAutore(): String{
        return autore?.trim() ?: "Anonimo"
    }
}