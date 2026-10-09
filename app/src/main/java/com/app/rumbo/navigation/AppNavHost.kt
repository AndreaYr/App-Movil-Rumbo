package com.app.rumbo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.rumbo.features.feed.FeedRoute
import com.app.rumbo.features.publicationdetail.PublicationDetailRoute

/** Navegación principal: conecta todas las features. */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = FeedDestination,
        modifier = modifier
    ) {
        composable<FeedDestination> {
            FeedRoute(onPublicationClick = { id -> navController.navigate(PublicationDetailDestination(id)) })
        }
        composable<PublicationDetailDestination> {
            PublicationDetailRoute(onBack = navController::popBackStack)
        }
    }
}
