package org.example

import java.security.MessageDigest

/** Erro de validação da entrada do utilizador. */
class ErroEntrada(mensagem: String) : Exception(mensagem)

/** 150.0 -> "150" ; 12.5 -> "12.5" */
fun formatar(valor: Double): String =
    if (valor == Math.rint(valor)) valor.toLong().toString() else valor.toString()

fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
