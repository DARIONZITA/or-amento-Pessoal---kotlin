package org.example

/**
 * Utilizador (era a tua classe "user"): email + password, sessões
 * e o seu orçamento (lista de movimentos por data).
 */
class User(password: String) {

    companion object {
        const val ULTIMA_DATA = 999
    }

    private val passwordHash = password.sha256()
    private val _sessoes = mutableListOf<Sessao>()
    val sessoes: List<Sessao> get() = _sessoes

    // data -> movimentos dessa data (pela ordem de inserção)
    private val lista: MutableMap<Int, MutableList<Movimento>> = mutableMapOf()
    var minBalanco: Double = 0.0

    // ---------- sessões / autenticação ----------
    fun passwordCorresponde(password: String): Boolean = passwordHash == password.sha256()

    fun abrirSessao(): Sessao = Sessao().also { _sessoes.add(it) }

    fun terminarSessao(sessao: Sessao) {
        _sessoes.remove(sessao)
    }

    // ---------- movimentos ----------
    fun inserirMovimento(movs: List<Movimento>) {
        for (movie in movs) {
            lista.getOrPut(movie.data) { mutableListOf() }.add(movie)
        }
    }

    fun mostrarDatas(data: Int): List<Movimento> = lista[data] ?: emptyList()

    fun removerMovimento(data: Int, indice: Int): Boolean {
        val movimentos = lista[data] ?: return false
        if (indice !in movimentos.indices) return false
        movimentos.removeAt(indice)
        return true
    }

    // ---------- balanço ----------
    /** Balanço na data: soma de todos os movimentos de 0 até [data] (inclusive). */
    fun balancoDatas(data: Int): Double {
        var result = 0.0
        for (i in 0..data) {
            lista[i]?.let { movimentos ->
                for (movie in movimentos) {
                    result += if (movie.tipo == TipoMovimento.RECEITA) movie.montante else -movie.montante
                }
            }
        }
        return result
    }

    private fun todosOsBalancos(): DoubleArray = DoubleArray(ULTIMA_DATA + 1) { balancoDatas(it) }

    /** true se em nenhuma data o balanço fica abaixo do mínimo. */
    fun verificarBalanco(): Boolean = todosOsBalancos().all { it >= minBalanco }

    /**
     * Primeira data >= [data] em que uma despesa de valor [despesa] não invalida o orçamento,
     * ou null se nunca for possível. Uma despesa na data d reduz o balanço de d até 999,
     * por isso o menor balanço desse intervalo menos a despesa tem de ser >= minBalanco.
     */
    fun adicionarDispesa(data: Int, despesa: Double): Int? {
        val balancos = todosOsBalancos()
        var menorDaquiParaFrente = Double.MAX_VALUE
        var resultado: Int? = null
        for (d in ULTIMA_DATA downTo data) {
            menorDaquiParaFrente = minOf(menorDaquiParaFrente, balancos[d])
            if (menorDaquiParaFrente - despesa >= minBalanco) resultado = d else break
        }
        return resultado
    }
}
