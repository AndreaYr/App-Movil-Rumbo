package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

/**
 * Catálogo de logros. Cada uno se mide con una cifra de [ActivityStats] ([metric]) y se
 * desbloquea al llegar a [target]. Al desbloquearse suma [points], y los puntos definen el [Level].
 */
enum class Achievement(
    @param:StringRes val displayNameRes: Int,
    @param:StringRes val descriptionRes: Int,
    val points: Int,
    val target: Int,
    val metric: (ActivityStats) -> Int
) {
    // Publicaciones
    FIRST_PUBLICATION(R.string.achievement_first_publication_name, R.string.achievement_first_publication_desc, 25, 1, { it.publications }),
    TEN_VERIFIED(R.string.achievement_ten_verified_name, R.string.achievement_ten_verified_desc, 100, 10, { it.verifiedPublications }),
    LOCAL_GUIDE(R.string.achievement_local_guide_name, R.string.achievement_local_guide_desc, 250, 25, { it.verifiedPublications }),

    // Visitas
    FIRST_VISIT(R.string.achievement_first_visit_name, R.string.achievement_first_visit_desc, 10, 1, { it.visitedPlaces }),
    WALKER(R.string.achievement_walker_name, R.string.achievement_walker_desc, 40, 10, { it.visitedPlaces }),
    CITY_EXPLORER(R.string.achievement_city_explorer_name, R.string.achievement_city_explorer_desc, 150, 50, { it.visitedPlaces }),

    // Comunidad
    TEN_COMMENTS(R.string.achievement_ten_comments_name, R.string.achievement_ten_comments_desc, 40, 10, { it.commentsMade }),
    COMMUNITY_VOICE(R.string.achievement_community_voice_name, R.string.achievement_community_voice_desc, 100, 50, { it.commentsMade }),
    FIFTY_VOTES(R.string.achievement_fifty_votes_name, R.string.achievement_fifty_votes_desc, 75, 50, { it.votesReceived }),
    COMMUNITY_FAVORITE(R.string.achievement_community_favorite_name, R.string.achievement_community_favorite_desc, 200, 200, { it.votesReceived }),

    // Rutas
    ROUTE_CREATOR(R.string.achievement_route_creator_name, R.string.achievement_route_creator_desc, 50, 1, { it.routesCreated }),
    ROUTE_MASTER(R.string.achievement_route_master_name, R.string.achievement_route_master_desc, 100, 5, { it.routesCreated }),
    ROUTE_COMPLETED(R.string.achievement_route_completed_name, R.string.achievement_route_completed_desc, 30, 1, { it.routesCompleted }),

    // Reconocimiento
    MONTH_HIGHLIGHT(R.string.achievement_month_highlight_name, R.string.achievement_month_highlight_desc, 300, 1, { it.monthlyTopPublications })
}
