package org.example

data class Movimento(
    var montante: Double,
    var data: Int,
    var tipo: TipoMovimento,
    var descricao: String
)
