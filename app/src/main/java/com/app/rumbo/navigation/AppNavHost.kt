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
import androidx.navigation.toRoute
import com.app.rumbo.core.component.RumboScaffold
import com.app.rumbo.core.component.RumboTab
import com.app.rumbo.features.achievements.AchievementsRoute
import com.app.rumbo.features.createpublication.CreatePublicationRoute
import com.app.rumbo.features.editprofile.EditProfileRoute
import com.app.rumbo.features.feed.FeedRoute
import com.app.rumbo.features.home.HomeRoute
import com.app.rumbo.features.login.LoginRoute
import com.app.rumbo.features.loginmoderator.LoginModeratorRoute
import com.app.rumbo.features.moderatoraccount.ModeratorAccountRoute
import com.app.rumbo.features.moderatordashboard.ModeratorDashboardRoute
import com.app.rumbo.features.moderationfinished.ModerationFinishedRoute
import com.app.rumbo.features.pendingpublications.PendingPublicationsRoute
import com.app.rumbo.features.rejectpublication.RejectPublicationRoute
import com.app.rumbo.features.reviewpublication.ReviewPublicationRoute
import com.app.rumbo.features.mypublications.MyPublicationsRoute
import com.app.rumbo.features.notifications.NotificationsRoute
import com.app.rumbo.features.passwordrecovery.PasswordRecoveryRoute
import com.app.rumbo.features.profile.ProfileRoute
import com.app.rumbo.features.publicationdetail.PublicationDetailRoute
import com.app.rumbo.features.publicationsubmitted.PublicationSubmittedRoute
import com.app.rumbo.features.register.RegisterRoute
import com.app.rumbo.features.splash.SplashRoute

private const val REGISTER_SUCCESS_RESULT_KEY = "register_success_message"
private const val REGISTER_SUCCESS_MESSAGE = "Registro de prueba completado. Ya puedes iniciar sesión"

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SplashDestination,
        modifier = modifier
    ) {
        composable<SplashDestination> {
            SplashRoute(
                onFinished = {
                    navController.navigate(LoginDestination) {
                        popUpTo(SplashDestination) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<HomeDestination> {
            RumboScaffold(
                selectedTab = RumboTab.HOME,
                onTabSelected = { navigateToTab(navController, it) }
            ) { padding ->
                HomeRoute(
                    onSearchClick = { navigateToTab(navController, RumboTab.EXPLORE) },
                    onPublicationClick = { id -> navController.navigate(PublicationDetailDestination(id)) },
                    onNotificationsClick = { navigateToTab(navController, RumboTab.ALERTS) },
                    // Se conecta cuando exista la pantalla del planificador
                    onPlanRouteClick = {},
                    modifier = Modifier.padding(padding)
                )
            }
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
        composable<NotificationsDestination> {
            RumboScaffold(
                selectedTab = RumboTab.ALERTS,
                onTabSelected = { navigateToTab(navController, it) }
            ) { padding ->
                NotificationsRoute(
                    onPublicationClick = { id -> navController.navigate(PublicationDetailDestination(id)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable<ProfileDestination> {
            RumboScaffold(
                selectedTab = RumboTab.PROFILE,
                onTabSelected = { navigateToTab(navController, it) }
            ) { padding ->
                ProfileRoute(
                    onMyPublicationsClick = { navController.navigate(MyPublicationsDestination) },
                    onBadgesClick = { navController.navigate(AchievementsDestination) },
                    onEditProfileClick = { navController.navigate(EditProfileDestination) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable<EditProfileDestination> {
            EditProfileRoute(onBack = navController::popBackStack)
        }
        composable<AchievementsDestination> {
            AchievementsRoute(onBack = navController::popBackStack)
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
                        popUpTo(HomeDestination) { inclusive = false }
                    }
                },
                onGoHome = {
                    navController.navigate(HomeDestination) {
                        popUpTo(HomeDestination) { inclusive = true }
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
                onModeratorClick = { navController.navigate(LoginModeratorDestination) },
                registrationMessage = registrationMessage,
                onRegistrationMessageConsumed = {
                    entry.savedStateHandle[REGISTER_SUCCESS_RESULT_KEY] = null
                },
                onLoginSuccess = {
                    navController.navigate(HomeDestination) {
                        popUpTo<LoginDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<LoginModeratorDestination> {
            LoginModeratorRoute(
                onLoginSuccess = {
                    navController.navigate(ModeratorDashboardDestination) {
                        popUpTo<LoginDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToUserLogin = { navController.popBackStack() }
            )
        }
        composable<ModeratorDashboardDestination> {
            ModeratorDashboardRoute(
                onStartReview = { navController.navigate(PendingPublicationsDestination) },
                onNavigateToPending = { navController.navigate(PendingPublicationsDestination) },
                onNavigateToVerified = {},
                onNavigateToRejected = {},
                onNavigateToFinished = {},
                onNavigateToHistory = {},
                onNavigateToAccount = { navController.navigate(ModeratorAccountDestination) },
                onLogout = {
                    navController.navigate(LoginDestination) {
                        popUpTo(ModeratorDashboardDestination) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<PendingPublicationsDestination> {
            PendingPublicationsRoute(
                onBack = { navController.popBackStack() },
                onFilterClick = {},
                onVerifyClick = { id -> navController.navigate(ReviewPublicationDestination(id)) },
                onRejectClick = { id -> navController.navigate(RejectPublicationDestination(id)) },
                onNavigateToPanel = { navController.navigate(ModeratorDashboardDestination) { launchSingleTop = true } },
                onNavigateToPending = { },
                onNavigateToHistory = {},
                onNavigateToAccount = { navController.navigate(ModeratorAccountDestination) }
            )
        }
        composable<ModeratorAccountDestination> {
            ModeratorAccountRoute(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(LoginDestination) {
                        popUpTo(ModeratorDashboardDestination) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<ReviewPublicationDestination> { backStackEntry ->
            val route = backStackEntry.toRoute<ReviewPublicationDestination>()
            ReviewPublicationRoute(
                onBack = { navController.popBackStack() },
                onVerify = {
                    navController.navigate(ModerationFinishedDestination) { popUpTo(PendingPublicationsDestination) { inclusive = false } }
                    navController.currentBackStackEntry?.savedStateHandle?.set("finished_title", "Publicación aprobada")
                    navController.currentBackStackEntry?.savedStateHandle?.set("finished_message", "La publicación ya está visible en el feed público.")
                },
                onReject = { id -> navController.navigate(RejectPublicationDestination(id)) }
            )
        }
        composable<RejectPublicationDestination> { backStackEntry ->
            val route = backStackEntry.toRoute<RejectPublicationDestination>()
            RejectPublicationRoute(
                onBack = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onConfirmReject = {
                    navController.navigate(ModerationFinishedDestination) { popUpTo(PendingPublicationsDestination) { inclusive = false } }
                    navController.currentBackStackEntry?.savedStateHandle?.set("finished_title", "Publicación rechazada")
                    navController.currentBackStackEntry?.savedStateHandle?.set("finished_message", "Se notificó al autor con el motivo del rechazo.")
                }
            )
        }
        composable<ModerationFinishedDestination> { entry ->
            val title = entry.savedStateHandle.get<String>("finished_title")
            val message = entry.savedStateHandle.get<String>("finished_message")
            ModerationFinishedRoute(
                title = title,
                message = message,
                onBackToPanel = {
                    navController.navigate(ModeratorDashboardDestination) {
                        popUpTo(ModeratorDashboardDestination) { inclusive = true }
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
        RumboTab.HOME -> navController.popBackStack(HomeDestination, inclusive = false)
        RumboTab.EXPLORE -> navigateToTopLevel(navController, FeedDestination)
        RumboTab.CREATE -> navigateToTopLevel(navController, CreatePublicationDestination)
        RumboTab.ALERTS -> navigateToTopLevel(navController, NotificationsDestination)
        RumboTab.PROFILE -> navigateToTopLevel(navController, ProfileDestination)
    }
}

/** Las pestañas cuelgan de Inicio: al cambiar de una a otra no se acumulan pantallas en la pila. */
private fun navigateToTopLevel(navController: NavHostController, destination: Any) {
    navController.navigate(destination) {
        popUpTo(HomeDestination) { inclusive = false }
        launchSingleTop = true
    }
}
