package com.app.rumbo.domain.model

/** Un turno de la conversación con el asistente, para que la IA tenga el contexto de lo ya dicho. */
data class ChatTurn(val fromUser: Boolean, val text: String)
