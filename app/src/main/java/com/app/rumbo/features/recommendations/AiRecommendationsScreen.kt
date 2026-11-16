package com.app.rumbo.features.recommendations

import com.app.rumbo.core.theme.DarkCoralText
import com.app.rumbo.core.theme.adaptiveColor
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.component.RumboScaffold
import com.app.rumbo.core.component.RumboTab
import com.app.rumbo.core.component.color
import com.app.rumbo.core.component.iconRes
import com.app.rumbo.core.util.formatKm
import com.app.rumbo.core.util.label
import com.app.rumbo.domain.model.AiRecommendation
import com.app.rumbo.domain.model.Publication

private val Accent: Color @Composable get() = adaptiveColor(Color(0xFFC2431E), DarkCoralText)
@Composable
fun AiRecommendationsRoute(
    onTabSelected: (RumboTab) -> Unit,
    onPublicationClick: (String) -> Unit,
    viewModel: AiRecommendationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var asking by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val routeSavedToast = stringResource(R.string.ai_route_saved_toast)
    val routeSaveFailedToast = stringResource(R.string.ai_route_save_failed)
    RumboScaffold(
        selectedTab = RumboTab.HOME,
        onTabSelected = onTabSelected,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { asking = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(painterResource(R.drawable.ic_smart_toy), contentDescription = stringResource(R.string.ai_ask_content_description), modifier = Modifier.size(26.dp))
            }
        }
    ) { padding ->
        AiRecommendationsScreen(
            state = state,
            onPublicationClick = onPublicationClick,
            onSaveRoute = { id ->
                viewModel.saveSuggestedRoute(id) { saved ->
                    Toast.makeText(
                        context,
                        if (saved) routeSavedToast else routeSaveFailedToast,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.padding(padding)
        )
    }
    if (asking) {
        AskSheet(
            enabled = !state.isThinking,
            onSend = {
                viewModel.send(it)
                asking = false
            },
            onDismiss = { asking = false }
        )
    }
}

@Composable
fun AiRecommendationsScreen(
    state: AiRecommendationsUiState,
    onPublicationClick: (String) -> Unit,
    onSaveRoute: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    // Cada mensaje nuevo (o el aviso "pensando") se deja a la vista al final de la lista
    LaunchedEffect(state.messages.size, state.isThinking) {
        val last = listState.layoutInfo.totalItemsCount - 1
        if (last > 1) listState.animateScrollToItem(last)
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header(state.userName, state.city) }
        if (state.isLoading) {
            item { ThinkingBubble(stringResource(R.string.home_for_you_loading)) }
        }
        items(state.messages, key = { it.id }) { message ->
            if (message.fromUser) {
                UserBubble(message.text)
            } else {
                AiMessage(
                    message = message,
                    routeSaved = message.id in state.savedRouteMessageIds,
                    onPublicationClick = onPublicationClick,
                    onSaveRoute = { onSaveRoute(message.id) }
                )
            }
        }
        if (state.isThinking) {
            item { ThinkingBubble("Pensando en tu plan…") }
        }
    }
}

@Composable
private fun Header(userName: String, city: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        if (userName.isNotBlank()) {
            Text(stringResource(R.string.ai_greeting_user, userName), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(stringResource(R.string.ai_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 3.dp))
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
}

@Composable
private fun UserBubble(text: String) {
    Box(Modifier.fillMaxWidth().padding(start = 56.dp, end = 16.dp), contentAlignment = Alignment.CenterEnd) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun ThinkingBubble(text: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Accent)
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AiMessage(
    message: ChatMessage,
    routeSaved: Boolean,
    onPublicationClick: (String) -> Unit,
    onSaveRoute: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painterResource(R.drawable.ic_auto_awesome), null, Modifier.size(16.dp), tint = Accent)
            Text(stringResource(R.string.ai_assistant_name), style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), color = Accent)
        }
        Text(
            message.textRes?.let { stringResource(it) } ?: message.text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = adaptiveColor(Color(0xFF6E4034), Color(0xFFE8B8A8)),
            modifier = Modifier
                .fillMaxWidth()
                .background(adaptiveColor(Color(0xFFFDEEE8), Color(0xFF3A2620)), RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .border(1.dp, adaptiveColor(Color(0xFFF6D3C6), Color(0xFF5A3A2F)), RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
        message.places.forEach { PlaceCard(it, onClick = { onPublicationClick(it.publication.id) }) }
        if (message.route.isNotEmpty()) SuggestedRoute(message.route, routeSaved, onPublicationClick, onSaveRoute)
    }
}

/** Tarjeta pequeña de un lugar recomendado, con el porqué que dio la IA. */
@Composable
private fun PlaceCard(pick: AiRecommendation, onClick: () -> Unit) {
    val place = pick.publication
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = Modifier
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
            Modifier.size(40.dp).background(place.category.color.copy(alpha = 0.13f), MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(place.category.iconRes), null, Modifier.size(20.dp), tint = place.category.color)
        }
        Column(Modifier.weight(1f)) {
            Text(place.title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${place.town} · ${place.distanceKm.formatKm()} · ${place.price.label}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (pick.reason.isNotBlank()) {
                Text(
                    pick.reason,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = adaptiveColor(Color(0xFF6E4034), Color(0xFFE8B8A8)),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SuggestedRoute(
    route: List<Publication>,
    saved: Boolean,
    onPublicationClick: (String) -> Unit,
    onSaveRoute: () -> Unit
) {
    val context = LocalContext.current
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painterResource(R.drawable.ic_route), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(stringResource(R.string.ai_suggested_route), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        route.forEachIndexed { index, place ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onPublicationClick(place.id) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(22.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = Color.White)
                }
                Text(place.title, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Button(onClick = onSaveRoute, enabled = !saved, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (saved) R.string.ai_route_saved else R.string.ai_route_save))
        }
        OutlinedButton(onClick = { context.openInMaps(route) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.ai_open_in_maps))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskSheet(enabled: Boolean, onSend: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.ic_smart_toy), null, Modifier.size(22.dp), tint = Accent)
                Text(stringResource(R.string.ai_ask_title), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                stringResource(R.string.ai_ask_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.ai_ask_placeholder)) },
                minLines = 2,
                maxLines = 5,
                trailingIcon = {
                    IconButton(onClick = { onSend(text) }, enabled = enabled && text.isNotBlank()) {
                        Icon(painterResource(R.drawable.ic_send), contentDescription = stringResource(R.string.ai_send), tint = if (enabled && text.isNotBlank()) Accent else Color.Gray)
                    }
                }
            )
        }
    }
}

/** Abre el recorrido en Google Maps (app o navegador) saliendo de la ubicación del usuario. */
private fun Context.openInMaps(route: List<Publication>) {
    val points = route.map { "${it.latitude},${it.longitude}" }
    val url = buildString {
        append("https://www.google.com/maps/dir/?api=1&destination=").append(points.last())
        if (points.size > 1) append("&waypoints=").append(Uri.encode(points.dropLast(1).joinToString("|")))
    }
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.detail_no_maps_app), Toast.LENGTH_SHORT).show()
    }
}
