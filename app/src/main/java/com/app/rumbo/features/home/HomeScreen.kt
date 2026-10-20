package com.app.rumbo.features.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.component.PublicationPhoto
import com.app.rumbo.core.component.color
import com.app.rumbo.core.component.iconRes
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.formatKm
import com.app.rumbo.core.util.formatThousands
import com.app.rumbo.core.util.toTimeAgo
import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.HomeContent
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.Trend

@Composable
fun HomeRoute(
    onSearchClick: () -> Unit,
    onPublicationClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onPlanRouteClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onSearchClick = onSearchClick,
        onPublicationClick = onPublicationClick,
        onNotificationsClick = onNotificationsClick,
        onPlanRouteClick = onPlanRouteClick,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onSearchClick: () -> Unit,
    onPublicationClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onPlanRouteClick: () -> Unit,
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
        item { HomeHeader(state.userName, state.city, state.hasUnreadNotifications, onNotificationsClick) }
        item { SearchField(onSearchClick, Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) }
        item { PlanRouteBanner(onPlanRouteClick, Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp)) }

        item { SectionHeader("Cerca de ti", actionLabel = "Ver todo", onAction = onSearchClick) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                items(state.content.nearby, key = { it.id }) { NearbyCard(it, onClick = { onPublicationClick(it.id) }) }
            }
        }

        item { SectionHeader("Más destacados") }
        items(state.content.topRated, key = { "top-${it.id}" }) {
            TopRatedRow(it, onClick = { onPublicationClick(it.id) }, Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp))
        }

        item { SectionHeader("Tendencias", subtitle = "esta semana") }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                items(state.content.trending, key = { it.publication.id }) {
                    TrendCard(it, onClick = { onPublicationClick(it.publication.id) })
                }
            }
        }

        item { SectionHeader("Nuevos lugares") }
        items(state.content.fresh, key = { "fresh-${it.id}" }) {
            FreshRow(it, onClick = { onPublicationClick(it.id) }, Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp))
        }
    }
}

@Composable
private fun HomeHeader(userName: String, city: String, hasUnread: Boolean, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            if (userName.isNotBlank()) {
                Text("Hola, $userName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("¿Hacia dónde\nvamos hoy?", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 3.dp))
            if (city.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_near_me), null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(city, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        NotificationsButton(hasUnread, onNotificationsClick)
    }
}

@Composable
private fun NotificationsButton(hasUnread: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(role = Role.Button, onClickLabel = "Ver notificaciones", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(R.drawable.ic_notifications), contentDescription = "Notificaciones", modifier = Modifier.size(21.dp))
        if (hasUnread) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 9.dp, end = 10.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

/** Falso buscador: al tocarlo lleva a Explorar, donde están los filtros. */
@Composable
private fun SearchField(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(painterResource(R.drawable.ic_search), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Busca lugares, comida, miradores…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PlanRouteBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(44.dp).background(Color.White.copy(alpha = 0.16f), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_route), null, Modifier.size(24.dp), tint = Color.White)
        }
        Column(Modifier.weight(1f)) {
            Text("Planifica tu ruta", style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(
                "Dinos tu tiempo y presupuesto y armamos el recorrido",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = Color(0xFFB6D8D1),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(painterResource(R.drawable.ic_arrow_forward), null, Modifier.size(22.dp), tint = Color.White)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (actionLabel != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .clickable(role = Role.Button, onClick = onAction),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun CategoryPill(category: Category, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.94f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.size(6.dp).background(category.color, CircleShape))
        Text(category.label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.5.sp))
    }
}

@Composable
private fun InfoLine(publication: Publication, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(R.drawable.ic_near_me), null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "${publication.distanceKm.formatKm()} · ${publication.price.label}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Interested(votes: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(R.drawable.ic_star), null, Modifier.size(14.dp), tint = Color(0xFFB4841A))
        Text("${votes.formatThousands()} interesados", style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = Color(0xFFB4841A))
    }
}

@Composable
private fun NearbyCard(publication: Publication, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .width(208.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
    ) {
        Box {
            PublicationPhoto(
                photo = publication.photos.firstOrNull(),
                fallbackLabel = publication.title,
                tint = publication.category.color,
                modifier = Modifier.fillMaxWidth().height(116.dp)
            )
            CategoryPill(publication.category, Modifier.align(Alignment.TopStart).padding(9.dp))
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(9.dp)
                    .size(24.dp)
                    .background(Color.White.copy(alpha = 0.94f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_verified), "Verificada", Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 11.dp, bottom = 13.dp)) {
            Text(publication.title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            InfoLine(publication, Modifier.padding(top = 6.dp))
            Interested(publication.votes, Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun TopRatedRow(publication: Publication, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PublicationPhoto(
            photo = publication.photos.firstOrNull(),
            fallbackLabel = publication.title,
            tint = publication.category.color,
            modifier = Modifier.size(78.dp).clip(RoundedCornerShape(13.dp))
        )
        Column(Modifier.weight(1f).align(Alignment.CenterVertically)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(6.dp).background(publication.category.color, CircleShape))
                Text(publication.category.label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(publication.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 5.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${publication.distanceKm.formatKm()} · ${publication.price.label}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(painterResource(R.drawable.ic_star), null, Modifier.size(13.dp), tint = Color(0xFFB4841A))
                    Text(publication.votes.formatThousands(), style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = Color(0xFFB4841A))
                }
            }
        }
    }
}

@Composable
private fun TrendCard(trend: Trend, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    val publication = trend.publication
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painterResource(R.drawable.ic_trending_up), null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.secondary)
            Text("+${trend.growthPercent}%", style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.secondary)
        }
        Text(publication.title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp), modifier = Modifier.padding(top = 8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            "${publication.category.label} · ${publication.distanceKm.formatKm()}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun FreshRow(publication: Publication, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    val tint = publication.category.color
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(40.dp).background(tint.copy(alpha = 0.13f), MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(publication.category.iconRes), null, Modifier.size(20.dp), tint = tint)
        }
        Column(Modifier.weight(1f)) {
            Text(publication.title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "Publicado por ${publication.author.name} · ${publication.publishedAt.toTimeAgo()}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(painterResource(R.drawable.ic_schedule), null, Modifier.size(12.dp), tint = Color(0xFF8A6A0B))
            Text("Nuevo", style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.5.sp), color = Color(0xFF8A6A0B))
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HomeScreenPreview() {
    val publications = SamplePublications.create()
    RumboTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                userName = "Mariana",
                city = "Armenia, Quindío",
                hasUnreadNotifications = true,
                content = HomeContent(
                    nearby = publications.take(3),
                    topRated = publications.take(2),
                    trending = publications.take(3).mapIndexed { i, p -> Trend(p, 64 - i * 20) },
                    fresh = publications.takeLast(2)
                )
            ),
            onSearchClick = {},
            onPublicationClick = {},
            onNotificationsClick = {},
            onPlanRouteClick = {}
        )
    }
}
