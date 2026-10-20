package org.example

import java.time.LocalDateTime
import java.util.UUID

/** Sessão: token único + data/hora de início. */
data class Sessao(
    val token: String = UUID.randomUUID().toString(),
    val dataHora: LocalDateTime = LocalDateTime.now()
)
