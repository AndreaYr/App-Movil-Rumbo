package com.app.rumbo.domain.model

/**
 * Catálogo de logros. Cada uno se mide con una cifra de [ActivityStats] ([metric]) y se
 * desbloquea al llegar a [target]. Al desbloquearse suma [points], y los puntos definen el [Level].
 */
enum class Achievement(
    val displayName: String,
    val description: String,
    val points: Int,
    val target: Int,
    val metric: (ActivityStats) -> Int
) {
    // Publicaciones
    FIRST_PUBLICATION("Primera publicación", "Comparte tu primer punto de interés.", 25, 1, { it.publications }),
    TEN_VERIFIED("10 publicaciones verificadas", "Consigue que un moderador apruebe 10 de tus publicaciones.", 100, 10, { it.verifiedPublications }),
    LOCAL_GUIDE("Guía local", "Llega a 25 publicaciones verificadas.", 250, 25, { it.verifiedPublications }),

    // Visitas
    FIRST_VISIT("Primera visita", "Marca tu primer lugar como visitado.", 10, 1, { it.visitedPlaces }),
    WALKER("Caminante", "Marca como visitados 10 lugares distintos.", 40, 10, { it.visitedPlaces }),
    CITY_EXPLORER("Explorador de la ciudad", "Marca como visitados 50 lugares distintos del Quindío.", 150, 50, { it.visitedPlaces }),

    // Comunidad
    TEN_COMMENTS("10 comentarios", "Comenta en 10 publicaciones.", 40, 10, { it.commentsMade }),
    COMMUNITY_VOICE("Voz de la comunidad", "Llega a 50 comentarios.", 100, 50, { it.commentsMade }),
    FIFTY_VOTES("50 votos recibidos", "Recibe 50 votos \"Me interesa\" en tus publicaciones.", 75, 50, { it.votesReceived }),
    COMMUNITY_FAVORITE("Favorito de la comunidad", "Recibe 200 votos \"Me interesa\".", 200, 200, { it.votesReceived }),

    // Rutas
    ROUTE_CREATOR("Creadora de rutas", "Crea tu primera ruta.", 50, 1, { it.routesCreated }),
    ROUTE_MASTER("Trazadora de rutas", "Crea 5 rutas.", 100, 5, { it.routesCreated }),
    ROUTE_COMPLETED("Ruta cumplida", "Completa una de tus rutas.", 30, 1, { it.routesCompleted }),

    // Reconocimiento
    MONTH_HIGHLIGHT("Lugar más destacado del mes", "Ten la publicación con más votos del mes en cualquier categoría.", 300, 1, { it.monthlyTopPublications })
}
