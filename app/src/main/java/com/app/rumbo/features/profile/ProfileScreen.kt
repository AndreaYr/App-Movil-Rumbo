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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.Coral
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.formatThousands
import com.app.rumbo.domain.model.Achievement
import com.app.rumbo.domain.model.Badge

@Composable
fun ProfileRoute(
    onMyPublicationsClick: () -> Unit,
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
    onBadgesClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
    ) {
        item { ProfileHeader(onEditProfileClick) }
        item { LevelCard(state, Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) }
        item { StatsRow(state, Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) }
        item { BadgesHeader(state.totalBadges, onBadgesClick) }
        item { ProfileLinks(state, onMyPublicationsClick, onBadgesClick, onLogoutClick, Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) }
    }
}

@Composable
private fun ProfileHeader(onEditClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Mi perfil", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderButton(R.drawable.ic_edit, "Editar perfil", onEditClick)
            // Ajustes todavía no tiene pantalla
            HeaderButton(R.drawable.ic_settings, "Ajustes")
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
                        "NIVEL ${state.levelNumber} · ${state.levelName.uppercase()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
                        color = Color.White
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                "${state.points.formatThousands()} puntos",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = Color(0xFFB6D8D1),
                modifier = Modifier.weight(1f)
            )
            if (state.nextLevelName != null) {
                Text(
                    "Siguiente: ${state.nextLevelName} · ${state.nextLevelPoints.formatThousands()}",
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
            if (state.nextLevelName != null) "Te faltan ${state.pointsToNext.formatThousands()} puntos para subir de nivel"
            else "¡Llegaste al nivel más alto!",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
            color = Color(0xFF9FCCC4),
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}

@Composable
private fun StatsRow(state: ProfileUiState, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        StatCard(R.drawable.ic_place, MaterialTheme.colorScheme.primary, state.publicationsCount, "Publicaciones", Modifier.weight(1f))
        StatCard(R.drawable.ic_check_circle, Color(0xFF2B6B45), state.visitedCount, "Lugares visitados", Modifier.weight(1f))
        StatCard(R.drawable.ic_route, Coral, state.routesCount, "Rutas creadas", Modifier.weight(1f))
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
        Text("Insignias", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(
            "Ver las $totalBadges",
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
private fun ProfileLinks(state: ProfileUiState, onMyPublicationsClick: () -> Unit, onBadgesClick: () -> Unit, onLogoutClick: () -> Unit, modifier: Modifier = Modifier) {
    val publicationsSub = if (state.pendingPublicationsCount > 0) {
        "${state.publicationsCount} · ${state.pendingPublicationsCount} pendientes de verificación"
    } else {
        "${state.publicationsCount} publicaciones"
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        ProfileLink(R.drawable.ic_library_books, "Mis publicaciones", publicationsSub, onMyPublicationsClick)
        // Rutas y estadísticas aún no tienen pantalla: se muestran sin acción
        ProfileLink(R.drawable.ic_route, "Mis rutas", "Aún no has creado rutas")
        ProfileLink(R.drawable.ic_military_tech, "Logros e insignias", "${state.unlockedBadges.size} de ${state.totalBadges} desbloqueadas", onBadgesClick)
        ProfileLink(R.drawable.ic_bar_chart, "Mis estadísticas", "Tu actividad en Rumbo")
        ProfileLink(R.drawable.ic_logout, "Cerrar sesión", "Salir de tu cuenta", onLogoutClick)
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
                levelName = "Exploradora",
                points = 240,
                nextLevelName = "Aventurera",
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
            onBadgesClick = {},
            onEditProfileClick = {},
            onLogoutClick = {}
        )
    }
}
