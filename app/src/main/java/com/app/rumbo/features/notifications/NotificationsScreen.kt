package com.app.rumbo.features.notifications

import com.app.rumbo.core.theme.DarkGoldText
import com.app.rumbo.core.theme.accentTeal
import com.app.rumbo.core.theme.adaptiveColor
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.ErrorContainer
import com.app.rumbo.core.theme.ErrorRed
import com.app.rumbo.core.theme.GoldContainer
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.theme.TealContainer
import com.app.rumbo.core.theme.TealDark
import com.app.rumbo.core.util.toTimeAgo
import com.app.rumbo.data.local.SampleNotifications
import com.app.rumbo.domain.model.Notification
import com.app.rumbo.domain.model.NotificationType
import com.app.rumbo.domain.model.NotificationsContent

private val UnreadBackground: Color @Composable get() = adaptiveColor(Color(0xFFEFF6F3), Color(0xFF16302B))
private val UnreadBorder: Color @Composable get() = adaptiveColor(Color(0xFFCFE3DD), Color(0xFF24504A))
private val ReadBorder: Color @Composable get() = adaptiveColor(Color(0xFFE9E3D8), Color(0xFF2E3B38))
private val ReadBadgeBackground: Color @Composable get() = adaptiveColor(Color(0xFFF0EBE1), Color(0xFF26312E))

@Composable
fun NotificationsRoute(
    onPublicationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationsScreen(
        state = state,
        onFilterSelected = viewModel::onFilterSelected,
        onMarkAllRead = viewModel::onMarkAllRead,
        onNotificationClick = { notification ->
            viewModel.onNotificationOpened(notification.id)
            notification.publicationId?.let(onPublicationClick)
        },
        modifier = modifier
    )
}

@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onFilterSelected: (NotificationFilter) -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val content = state.content
    val sectionToday = stringResource(R.string.notifications_section_today)
    val sectionWeek = stringResource(R.string.notifications_section_week)
    val sectionEarlier = stringResource(R.string.notifications_section_earlier)
    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
    ) {
        item { Header(canMarkAll = content.unreadCount > 0, onMarkAllRead = onMarkAllRead) }
        item {
            FilterChips(
                selected = state.filter,
                totalCount = content.totalCount,
                unreadCount = content.unreadCount,
                onSelected = onFilterSelected,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
        if (content.isEmpty) {
            item { EmptyState(unreadOnly = state.filter == NotificationFilter.UNREAD) }
        }
        section(sectionToday, content.today, onNotificationClick)
        section(sectionWeek, content.thisWeek, onNotificationClick)
        section(sectionEarlier, content.earlier, onNotificationClick)
    }
}

private fun LazyListScope.section(
    title: String,
    notifications: List<Notification>,
    onClick: (Notification) -> Unit
) {
    if (notifications.isEmpty()) return
    item(key = "header-$title") {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp)
        )
    }
    items(notifications, key = { it.id }) {
        NotificationCard(it, onClick = { onClick(it) }, Modifier.padding(start = 16.dp, end = 16.dp, top = 9.dp))
    }
}

@Composable
private fun Header(canMarkAll: Boolean, onMarkAllRead: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.notifications_title), style = MaterialTheme.typography.titleLarge)
        if (canMarkAll) {
            Text(
                stringResource(R.string.notifications_mark_all_read),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = accentTeal(),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onMarkAllRead)
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun FilterChips(
    selected: NotificationFilter,
    totalCount: Int,
    unreadCount: Int,
    onSelected: (NotificationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        FilterChip(stringResource(R.string.notifications_filter_all, totalCount), selected == NotificationFilter.ALL) { onSelected(NotificationFilter.ALL) }
        FilterChip(stringResource(R.string.notifications_filter_unread, unreadCount), selected == NotificationFilter.UNREAD) { onSelected(NotificationFilter.UNREAD) }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold),
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else ReadBorder, CircleShape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun NotificationCard(notification: Notification, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val style = notification.type.style()
    val shape = RoundedCornerShape(16.dp)
    val unread = !notification.isRead
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (unread) UnreadBackground else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (unread) UnreadBorder else ReadBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(13.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(
                Modifier.size(38.dp).background(style.container, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(style.icon), null, Modifier.size(20.dp), tint = style.content)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    notification.text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium
                    ),
                    modifier = Modifier.padding(end = if (unread) 10.dp else 0.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        notification.createdAt.toTimeAgo(),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ReadBadge(unread)
                }
            }
        }
        if (unread) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
            )
        }
    }
}

@Composable
private fun ReadBadge(unread: Boolean) {
    Text(
        if (unread) stringResource(R.string.notifications_badge_unread) else stringResource(R.string.notifications_badge_read),
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
        color = if (unread) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(if (unread) MaterialTheme.colorScheme.primary else ReadBadgeBackground, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
private fun EmptyState(unreadOnly: Boolean) {
    Text(
        if (unreadOnly) stringResource(R.string.notifications_empty_unread) else stringResource(R.string.notifications_empty),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp)
    )
}

/** Icono y colores de cada tipo de aviso, tal como en el prototipo. */
private class TypeStyle(@param:DrawableRes val icon: Int, val container: Color, val content: Color)

@Composable
private fun NotificationType.style(): TypeStyle = when (this) {
    NotificationType.PUBLICATION_VERIFIED -> TypeStyle(R.drawable.ic_verified, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    NotificationType.PUBLICATION_REJECTED -> TypeStyle(R.drawable.ic_cancel, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
    NotificationType.COMMENT -> TypeStyle(R.drawable.ic_chat_bubble, adaptiveColor(Color(0xFFEAF1FB), Color(0xFF1B2A3F)), adaptiveColor(Color(0xFF2B5FA8), Color(0xFF8DB4F0)))
    NotificationType.ACHIEVEMENT -> TypeStyle(R.drawable.ic_award, MaterialTheme.colorScheme.tertiaryContainer, adaptiveColor(Color(0xFF8A6A0B), Color(0xFFE3C26A)))
    NotificationType.LEVEL_UP -> TypeStyle(R.drawable.ic_person, adaptiveColor(Color(0xFFE9F1EE), Color(0xFF1D3A34)), MaterialTheme.colorScheme.onPrimaryContainer)
    NotificationType.TRENDING -> TypeStyle(R.drawable.ic_trending_up, MaterialTheme.colorScheme.tertiaryContainer, adaptiveColor(Color(0xFFB4841A), DarkGoldText))
    NotificationType.RELEVANT -> TypeStyle(R.drawable.ic_star, MaterialTheme.colorScheme.tertiaryContainer, adaptiveColor(Color(0xFFB4841A), DarkGoldText))
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    val all = SampleNotifications.create()
    RumboTheme {
        NotificationsScreen(
            state = NotificationsUiState(
                isLoading = false,
                content = NotificationsContent(
                    today = all.take(4),
                    thisWeek = all.drop(4),
                    totalCount = all.size,
                    unreadCount = all.count { !it.isRead }
                )
            ),
            onFilterSelected = {},
            onMarkAllRead = {},
            onNotificationClick = {}
        )
    }
}
