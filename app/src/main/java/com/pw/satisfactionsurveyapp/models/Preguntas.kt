package com.pw.satisfactionsurveyapp.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pregunta(
    val id: Long,
    @SerialName("texto_pregunta")
    val texto: String,

    @SerialName("opciones")
    val opciones: List<Opcion>
)

@Serializable
data class Opcion(
    val id: Long,
    @SerialName("texto_opcion")
    val texto: String,
    @SerialName("pregunta_id")
    val preguntaId: Long
)

@Serializable
data class RespuestaUsuario(
    @SerialName("pregunta_id") val preguntaId: Long,
    @SerialName("opcion_id") val opcionId: Long
)