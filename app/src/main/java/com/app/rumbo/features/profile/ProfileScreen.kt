package com.app.rumbo.features.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.component.LanguageDialog
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.displayName
import com.app.rumbo.core.util.formatThousands
import com.app.rumbo.domain.model.Achievement
import com.app.rumbo.domain.model.Badge
import com.app.rumbo.domain.model.Level

@Composable
fun ProfileRoute(
    onMyPublicationsClick: () -> Unit,
    onMyRoutesClick: () -> Unit,
    onBadgesClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onMyPublicationsClick = onMyPublicationsClick,
        onMyRoutesClick = onMyRoutesClick,
        onBadgesClick = onBadgesClick,
        onEditProfileClick = onEditProfileClick,
        onLogoutClick = onLogoutClick,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onMyPublicationsClick: () -> Unit,
    onMyRoutesClick: () -> Unit,
    onBadgesClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }
    if (state.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
    ) {
        item { ProfileHeader(onEditProfileClick, onSettingsClick = { showLanguageDialog = true }) }
        item { LevelCard(state, Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) }
        item { StatsRow(state, Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) }
        item { BadgesHeader(state.totalBadges, onBadgesClick) }
        item { ProfileLinks(state, onMyPublicationsClick, onMyRoutesClick, onBadgesClick, onLogoutClick, Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) }
    }
}

@Composable
private fun ProfileHeader(onEditClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderButton(R.drawable.ic_edit, stringResource(R.string.profile_edit), onEditClick)
            HeaderButton(R.drawable.ic_settings, stringResource(R.string.profile_settings), onSettingsClick)
        }
    }
}

@Composable
private fun HeaderButton(@DrawableRes icon: Int, description: String, onClick: (() -> Unit)? = null) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClickLabel = description, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), description, Modifier.size(19.dp))
    }
}

@Composable
private fun LevelCard(state: ProfileUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraLarge)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(Color.White.copy(alpha = 0.15f), MaterialTheme.shapes.large)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text(state.initials, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(state.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = Color.White)
                Row(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .background(Coral, CircleShape)
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_compass), null, Modifier.size(13.dp), tint = Color.White)
                    Text(
                        stringResource(R.string.profile_level_line, state.levelNumber, state.level.displayName.uppercase()),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
                        color = Color.White
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                stringResource(R.string.profile_points, state.points.formatThousands()),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = Color(0xFFB6D8D1),
                modifier = Modifier.weight(1f)
            )
            if (state.nextLevel != null) {
                Text(
                    stringResource(R.string.profile_next_level, state.nextLevel?.displayName.orEmpty(), state.nextLevelPoints.formatThousands()),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    color = Color(0xFFB6D8D1)
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(state.levelProgress)
                    .height(8.dp)
                    .background(Color(0xFFF2C94C), CircleShape)
            )
        }
        Text(
            if (state.nextLevel != null) stringResource(R.string.profile_points_to_next, state.pointsToNext.formatThousands())
            else stringResource(R.string.profile_max_level),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
            color = Color(0xFF9FCCC4),
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}

@Composable
private fun StatsRow(state: ProfileUiState, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        StatCard(R.drawable.ic_place, MaterialTheme.colorScheme.primary, state.publicationsCount, stringResource(R.string.profile_stat_publications), Modifier.weight(1f))
        StatCard(R.drawable.ic_check_circle, Color(0xFF2B6B45), state.visitedCount, stringResource(R.string.profile_stat_visited), Modifier.weight(1f))
        StatCard(R.drawable.ic_route, Coral, state.routesCount, stringResource(R.string.profile_stat_routes), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(@DrawableRes icon: Int, tint: Color, value: Int, label: String, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 11.dp, vertical = 13.dp)
    ) {
        Icon(painterResource(icon), null, Modifier.size(19.dp), tint = tint)
        Text(value.formatThousands(), style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp), modifier = Modifier.padding(top = 8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun BadgesHeader(totalBadges: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.profile_badges), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.profile_see_badges, totalBadges),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(vertical = 6.dp)
        )
    }
}

@Composable
private fun ProfileLinks(state: ProfileUiState, onMyPublicationsClick: () -> Unit, onMyRoutesClick: () -> Unit, onBadgesClick: () -> Unit, onLogoutClick: () -> Unit, modifier: Modifier = Modifier) {
    val publicationsSub = if (state.pendingPublicationsCount > 0) {
        stringResource(
            R.string.profile_publications_pending_subtitle,
            state.publicationsCount.formatThousands(),
            state.pendingPublicationsCount.formatThousands()
        )
    } else {
        stringResource(R.string.profile_publications_subtitle, state.publicationsCount.formatThousands())
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        ProfileLink(R.drawable.ic_library_books, stringResource(R.string.profile_my_publications), publicationsSub, onMyPublicationsClick)
        ProfileLink(
            R.drawable.ic_route,
            stringResource(R.string.profile_my_routes),
            when (state.routesCount) {
                0 -> stringResource(R.string.profile_routes_none)
                1 -> stringResource(R.string.profile_routes_one)
                else -> stringResource(R.string.profile_routes_many, state.routesCount)
            },
            onMyRoutesClick
        )
        // Estadísticas aún no tiene pantalla: se muestra sin acción
        ProfileLink(R.drawable.ic_military_tech, stringResource(R.string.profile_achievements), stringResource(R.string.profile_badges_subtitle, state.unlockedBadges.size, state.totalBadges), onBadgesClick)
        ProfileLink(R.drawable.ic_bar_chart, stringResource(R.string.profile_my_stats), stringResource(R.string.profile_my_stats_subtitle))
        ProfileLink(R.drawable.ic_logout, stringResource(R.string.profile_logout), stringResource(R.string.profile_logout_subtitle), onLogoutClick)
    }
}

@Composable
private fun ProfileLink(@DrawableRes icon: Int, title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .heightIn(min = 56.dp)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(painterResource(icon), null, Modifier.size(21.dp), tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
        Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(19.dp), tint = Color(0xFFB9C2BE))
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ProfileScreenPreview() {
    RumboTheme {
        ProfileScreen(
            state = ProfileUiState(
                isLoading = false,
                name = "Mariana Ospina",
                initials = "MO",
                levelNumber = 2,
                level = Level.EXPLORER,
                points = 240,
                nextLevel = Level.ADVENTURER,
                nextLevelPoints = 500,
                pointsToNext = 260,
                levelProgress = 0.26f,
                publicationsCount = 3,
                pendingPublicationsCount = 1,
                visitedCount = 12,
                unlockedBadges = listOf(
                    Badge(Achievement.FIRST_PUBLICATION, 3),
                    Badge(Achievement.FIRST_VISIT, 12),
                    Badge(Achievement.WALKER, 12)
                ),
                lockedBadgesCount = 11,
                totalBadges = 14
            ),
            onMyPublicationsClick = {},
            onMyRoutesClick = {},
            onBadgesClick = {},
            onEditProfileClick = {},
            onLogoutClick = {}
        )
    }
}
