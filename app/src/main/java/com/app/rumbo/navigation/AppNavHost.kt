package com.app.rumbo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.rumbo.core.component.RumboScaffold
import com.app.rumbo.core.component.RumboTab
import com.app.rumbo.features.createpublication.CreatePublicationRoute
import com.app.rumbo.features.feed.FeedRoute
import com.app.rumbo.features.mypublications.MyPublicationsDestination
import com.app.rumbo.features.mypublications.MyPublicationsRoute
import com.app.rumbo.features.publicationdetail.PublicationDetailRoute
import com.app.rumbo.features.publicationsubmitted.PublicationSubmittedDestination
import com.app.rumbo.features.publicationsubmitted.PublicationSubmittedRoute

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
            RumboScaffold(
                selectedTab = RumboTab.EXPLORE,
                onTabSelected = { navigateToTab(navController, it) }
            ) { padding ->
                FeedRoute(
                    onPublicationClick = { id -> navController.navigate(PublicationDetailDestination(id)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable<CreatePublicationDestination> {
            RumboScaffold(
                selectedTab = RumboTab.CREATE,
                onTabSelected = { navigateToTab(navController, it) }
            ) { padding ->
                CreatePublicationRoute(
                    onClose = navController::popBackStack,
                    onPublished = { title ->
                        navController.navigate(PublicationSubmittedDestination(title)) {
                            popUpTo(CreatePublicationDestination) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable<PublicationSubmittedDestination> {
            PublicationSubmittedRoute(
                onMyPublications = {
                    navController.navigate(MyPublicationsDestination) {
                        popUpTo(FeedDestination) { inclusive = false }
                    }
                },
                onGoHome = {
                    navController.navigate(FeedDestination) {
                        popUpTo(FeedDestination) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<MyPublicationsDestination> {
            MyPublicationsRoute(
                onBack = navController::popBackStack,
                onPublicationClick = { id -> navController.navigate(PublicationDetailDestination(id)) }
            )
        }
        composable<PublicationDetailDestination> {
            PublicationDetailRoute(onBack = navController::popBackStack)
        }
    }
}

/** Cambia de pestaña. Inicio, Avisos y Perfil aún no tienen pantalla propia. */
private fun navigateToTab(navController: NavHostController, tab: RumboTab) {
    when (tab) {
        RumboTab.EXPLORE -> {
            val popped = navController.popBackStack(FeedDestination, inclusive = false)
            if (!popped) {
                navController.navigate(FeedDestination) { launchSingleTop = true }
            }
        }
        RumboTab.CREATE -> navController.navigate(CreatePublicationDestination) {
            launchSingleTop = true
        }
        RumboTab.HOME, RumboTab.ALERTS, RumboTab.PROFILE -> Unit
    }
}
