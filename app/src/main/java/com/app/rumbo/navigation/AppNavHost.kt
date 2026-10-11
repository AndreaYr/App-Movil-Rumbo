package com.app.rumbo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.rumbo.core.component.RumboScaffold
import com.app.rumbo.core.component.RumboTab
import com.app.rumbo.features.createpublication.CreatePublicationRoute
import com.app.rumbo.features.feed.FeedRoute
import com.app.rumbo.features.login.LoginRoute
import com.app.rumbo.features.mypublications.MyPublicationsRoute
import com.app.rumbo.features.onboarding.OnboardingRoute
import com.app.rumbo.features.passwordrecovery.PasswordRecoveryRoute
import com.app.rumbo.features.publicationdetail.PublicationDetailRoute
import com.app.rumbo.features.publicationsubmitted.PublicationSubmittedRoute
import com.app.rumbo.features.register.RegisterRoute

private const val REGISTER_SUCCESS_RESULT_KEY = "register_success_message"
private const val REGISTER_SUCCESS_MESSAGE = "Registro de prueba completado. Ya puedes iniciar sesión"

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = OnboardingDestination,
        modifier = modifier
    ) {
        composable<OnboardingDestination> {
            OnboardingRoute(
                onContinueClick = {
                    navController.navigate(LoginDestination) {
                        popUpTo(OnboardingDestination) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
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
        composable<LoginDestination> { entry ->
            val registrationMessage by entry.savedStateHandle
                .getStateFlow<String?>(REGISTER_SUCCESS_RESULT_KEY, null)
                .collectAsStateWithLifecycle()
            LoginRoute(
                onRegisterClick = { navController.navigate(RegisterDestination) },
                onRecoveryClick = { navController.navigate(PasswordRecoveryDestination) },
                registrationMessage = registrationMessage,
                onRegistrationMessageConsumed = {
                    entry.savedStateHandle[REGISTER_SUCCESS_RESULT_KEY] = null
                },
                onLoginSuccess = {
                    navController.navigate(FeedDestination) {
                        popUpTo<LoginDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<RegisterDestination> {
            RegisterRoute(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(REGISTER_SUCCESS_RESULT_KEY, REGISTER_SUCCESS_MESSAGE)
                    navController.popBackStack()
                }
            )
        }
        composable<PasswordRecoveryDestination> {
            PasswordRecoveryRoute(onBack = navController::popBackStack)
        }
        composable<PublicationDetailDestination> {
            PublicationDetailRoute(onBack = navController::popBackStack)
        }
    }
}

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
