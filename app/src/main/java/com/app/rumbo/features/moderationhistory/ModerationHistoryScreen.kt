package com.app.rumbo.features.moderationhistory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.app.rumbo.R
import com.app.rumbo.core.component.ModeratorBottomBar
import com.app.rumbo.core.component.ModeratorTab
import com.app.rumbo.core.component.StatusChip
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.toTimeAgo
import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus

private val BodyBeige = Color(0xFFF7F5EE)
private val HeaderDark = Color(0xFF0F1E1B)
private val White = Color(0xFFFFFFFF)
private val BorderColor = Color(0xFFEFECE4)
private val VerifyGreen = Color(0xFF117361)
private val RejectOrange = Color(0xFFD95338)
private val TextDark = Color(0xFF0F1E1B)
private val TextSecondary = Color(0xFF6B7C75)
private val TextMeta = Color(0xFF828282)
private val ImagePlaceholder = Color(0xFFE6E2D6)
private val RejectedReasonBg = Color(0xFFFDECEA)
private val FilterCountBg = Color(0xFFEFECE4)
private val ActiveFilterBg = Color(0xFF23C49D)

@Composable
fun ModerationHistoryScreen(
    state: ModerationHistoryUiState,
    onBack: () -> Unit = {},
    onFilterSelected: (ModerationHistoryFilter) -> Unit = {},
    onPublicationClick: (String) -> Unit = {},
    onNavigateToPanel: () -> Unit = {},
    onNavigateToPending: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {}
) {
    Scaffold(
        containerColor = BodyBeige,
        bottomBar = {
            ModeratorBottomBar(
                selectedTab = ModeratorTab.HISTORY,
                onPanelClick = onNavigateToPanel,
                onPendingClick = onNavigateToPending,
                onHistoryClick = {},
                onAccountClick = onNavigateToAccount
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HistoryHeader(state = state, onBack = onBack)
            HistoryFilters(
                state = state,
                onFilterSelected = onFilterSelected
            )
            Spacer(Modifier.height(12.dp))
            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VerifyGreen)
                }

                state.publications.isEmpty() -> EmptyHistoryState(
                    filter = state.filter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.publications, key = { it.id }) { publication ->
                        ModerationHistoryCard(
                            publication = publication,
                            onClick = { onPublicationClick(publication.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    state: ModerationHistoryUiState,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(White)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Atrás",
                tint = TextDark,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Historial",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                text = "${state.approvedCount} aprobadas • ${state.rejectedCount} rechazadas",
                color = TextMeta,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun HistoryFilters(
    state: ModerationHistoryUiState,
    onFilterSelected: (ModerationHistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HistoryFilterChip(
            label = ModerationHistoryFilter.ALL.label,
            count = state.approvedCount + state.rejectedCount,
            selected = state.filter == ModerationHistoryFilter.ALL,
            onClick = { onFilterSelected(ModerationHistoryFilter.ALL) }
        )
        HistoryFilterChip(
            label = ModerationHistoryFilter.APPROVED.label,
            count = state.approvedCount,
            selected = state.filter == ModerationHistoryFilter.APPROVED,
            onClick = { onFilterSelected(ModerationHistoryFilter.APPROVED) }
        )
        HistoryFilterChip(
            label = ModerationHistoryFilter.REJECTED.label,
            count = state.rejectedCount,
            selected = state.filter == ModerationHistoryFilter.REJECTED,
            onClick = { onFilterSelected(ModerationHistoryFilter.REJECTED) }
        )
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) HeaderDark else White)
            .border(1.dp, if (selected) HeaderDark else BorderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (selected) White else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (selected) ActiveFilterBg else FilterCountBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                color = if (selected) HeaderDark else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ModerationHistoryCard(
    publication: Publication,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ImagePlaceholder),
                    contentAlignment = Alignment.Center
                ) {
                    if (publication.photos.isNotEmpty()) {
                        AsyncImage(
                            model = publication.photos.first().url,
                            contentDescription = publication.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_photo),
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(status = publication.status)
                        Text(
                            text = publication.publishedAt.toTimeAgo(),
                            color = TextMeta,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = publication.title,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${publication.category.label} • ${publication.town}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = null,
                            tint = TextMeta,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${publication.author.name} • ${publication.author.levelName}",
                            color = TextMeta,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            if (publication.status == PublicationStatus.REJECTED &&
                !publication.rejectionReason.isNullOrBlank()
            ) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RejectedReasonBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Motivo del rechazo",
                        color = RejectOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = publication.rejectionReason.orEmpty(),
                        color = TextDark,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    filter: ModerationHistoryFilter,
    modifier: Modifier = Modifier
) {
    val message = when (filter) {
        ModerationHistoryFilter.ALL -> "Todavía no hay publicaciones aprobadas ni rechazadas."
        ModerationHistoryFilter.APPROVED -> "Todavía no hay publicaciones aprobadas."
        ModerationHistoryFilter.REJECTED -> "Todavía no hay publicaciones rechazadas."
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_library_books),
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Sin publicaciones",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModerationHistoryPreview() {
    val history = SamplePublications.create()
        .filter { it.status == PublicationStatus.VERIFIED || it.status == PublicationStatus.REJECTED }
    RumboTheme {
        ModerationHistoryScreen(
            state = ModerationHistoryUiState(
                publications = history,
                approvedCount = history.count { it.status == PublicationStatus.VERIFIED },
                rejectedCount = history.count { it.status == PublicationStatus.REJECTED },
                isLoading = false
            )
        )
    }
}
