package org.example

/**
 * Era a tua classe "sistema": lê a linha, separa em elementos (next/lerData)
 * e chama a função do comando. Agora tem vários utilizadores e sessão.
 */
class Sistema {
    private val users: MutableMap<String, User> = mutableMapOf()
    private var userAtual: User? = null
    private var sessaoAtual: Sessao? = null

    var inputElemts: List<String> = listOf()
    var i: Int = 0

    // ---------- leitura da entrada ----------
    private fun next(): String {
        if (i >= inputElemts.size) throw ErroEntrada("Faltam argumentos")
        val input = inputElemts[i]
        i++
        return input
    }

    private fun lerInteiro(nome: String, min: Int, max: Int): Int {
        val valor = next().toIntOrNull()
        if (valor == null || valor < min || valor > max) {
            throw ErroEntrada("$nome deve ser um inteiro entre $min e $max")
        }
        return valor
    }

    private fun lerData(): Int = lerInteiro("data", 0, User.ULTIMA_DATA)

    private fun lerMontante(nome: String = "montante"): Double =
        next().toDoubleOrNull() ?: throw ErroEntrada("$nome deve ser um número")

    /** Lê uma linha da consola. Devolve false se a entrada acabou (Ctrl+D). */
    fun receberInput(): Boolean {
        val inpu: String = readlnOrNull() ?: return false
        inputElemts = inpu.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        i = 0
        return true
    }

    private fun userLogado(): User =
        userAtual ?: throw ErroEntrada("Sem sessão. Use: u email password")

    // ---------- comandos ----------
    /** u email password */
    fun iniciarSessao() {
        val email = next().lowercase()
        val password = next()
        if ('@' !in email) throw ErroEntrada("Email inválido")

        val existente = users[email]
        val user = when {
            existente == null -> User(password).also { users[email] = it }
            existente.passwordCorresponde(password) -> existente
            else -> throw ErroEntrada("Password incorreta")
        }
        userAtual = user
        sessaoAtual = user.abrirSessao()
    }

    /** exit: termina a sessão (não o programa) */
    fun terminarSessao() {
        val user = userLogado()
        sessaoAtual?.let { user.terminarSessao(it) }
        userAtual = null
        sessaoAtual = null
    }

    /** i montante data periodicidade tipo descricao */
    fun inserir() {
        val montante = lerMontante()
        val data = lerData()
        val periodicidade = lerInteiro("periodicidade", 0, User.ULTIMA_DATA)
        val tipo = when (next()) {
            "r" -> TipoMovimento.RECEITA
            "d" -> TipoMovimento.DESPESA
            else -> throw ErroEntrada("tipo deve ser 'r' ou 'd'")
        }
        val descricao = next()
        if (descricao.length > 60) throw ErroEntrada("descrição com mais de 60 caracteres")

        val arr = ArrayList<Movimento>()
        var i = 0
        while (data + periodicidade * i <= User.ULTIMA_DATA) {
            arr.add(Movimento(montante, data + periodicidade * i, tipo, descricao))
            i++
            if (periodicidade == 0) break
        }
        userLogado().inserirMovimento(arr)
    }

    /** l data */
    fun mostrar() {
        val data = lerData()
        for ((indice, m) in userLogado().mostrarDatas(data).withIndex()) {
            println("$indice ${formatar(m.montante)} ${m.tipo.simbolo} ${m.descricao}")
        }
    }

    /** r data indice */
    fun remover() {
        val data = lerData()
        val indice = lerInteiro("indice", 0, Int.MAX_VALUE)
        if (!userLogado().removerMovimento(data, indice)) {
            throw ErroEntrada("Não existe o movimento $indice na data $data")
        }
    }

    /** b data */
    fun balanco() {
        val data = lerData()
        println(formatar(userLogado().balancoDatas(data)))
    }

    /** m valor */
    fun minimoBalanco() {
        userLogado().minBalanco = lerMontante("minimo")
    }

    /** v */
    fun verificarBalanco() {
        println(if (userLogado().verificarBalanco()) "Valido" else "Invalido")
    }

    /** w data despesa */
    fun adicionarDispesa() {
        val data = lerData()
        val despesa = lerMontante("despesa")
        println(userLogado().adicionarDispesa(data, despesa) ?: "Incomportavel")
    }

    /** Identifica o comando e chama a função. Devolve false para terminar o programa (quit). */
    fun chamarFunc(): Boolean {
        when (next()) {
            "u" -> iniciarSessao()
            "exit" -> terminarSessao()
            "quit" -> return false
            "i" -> inserir()
            "l" -> mostrar()
            "r" -> remover()
            "b" -> balanco()
            "m" -> minimoBalanco()
            "v" -> verificarBalanco()
            "w" -> adicionarDispesa()
            else -> throw ErroEntrada("Comando desconhecido")
        }
        return true
    }

    fun executar() {
        while (receberInput()) {
            if (inputElemts.isEmpty()) continue
            try {
                if (!chamarFunc()) break
            } catch (e: ErroEntrada) {
                System.err.println("Erro: ${e.message}")
            }
        }
    }
}
