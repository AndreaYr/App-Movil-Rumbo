package com.app.rumbo.domain.model

enum class PublicationStatus(val label: String) {
    PENDING("Pendiente"),
    VERIFIED("Verificada"),
    REJECTED("Rechazada"),
    FINISHED("Finalizada")
}
