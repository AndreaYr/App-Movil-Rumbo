package com.app.rumbo.features.pendingpublications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus

private val BodyBeige = Color(0xFFF7F5EE)
private val HeaderDark = Color(0xFF0F1E1B)
private val White = Color(0xFFFFFFFF)
private val BorderColor = Color(0xFFEFECE4)
private val PendingBadgeBg = Color(0xFFFEF3C7)
private val PendingBadgeText = Color(0xFF9A7B1C)
private val VerifyGreen = Color(0xFF117361)
private val RejectOrange = Color(0xFFD95338)
private val TextDark = Color(0xFF0F1E1B)
private val TextSecondary = Color(0xFF6B7C75)
private val TextMeta = Color(0xFF828282)
private val ActiveNavGreen = Color(0xFF23C49D)
private val InactiveNav = Color(0xFF6B7C75)
private val ImagePlaceholder = Color(0xFFE6E2D6)

@Composable
fun PendingPublicationsScreen(
    state: PendingPublicationsUiState,
    onBack: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onVerifyClick: (String) -> Unit = {},
    onRejectClick: (String) -> Unit = {},
    onNavigateToPanel: () -> Unit = {},
    onNavigateToPending: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {}
) {
    Scaffold(
        containerColor = BodyBeige,
        bottomBar = {
            ModeratorBottomBarPending(
                selected = "pendientes",
                onNavigateToPanel = onNavigateToPanel,
                onNavigateToPending = onNavigateToPending,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToAccount = onNavigateToAccount
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Pendientes",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "${state.pendingCount} por revisar • más antiguas primero",
                        color = TextMeta,
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(White)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sort),
                        contentDescription = "Filtrar",
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                items(state.publications) { publication ->
                    PendingPublicationCard(
                        publication = publication,
                        onVerifyClick = { onVerifyClick(publication.id) },
                        onRejectClick = { onRejectClick(publication.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingPublicationCard(
    publication: Publication,
    onVerifyClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
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
                // Imagen
                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                        modifier = Modifier.size(32.dp)
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(PendingBadgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PENDIENTE",
                                color = PendingBadgeText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "hace 2 d",
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
                        text = "${publication.category.name} • ${publication.town}",
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
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    Button(
                        onClick = onRejectClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RejectOrange)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cancel),
                            contentDescription = null,
                            tint = RejectOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Rechazar",
                            color = RejectOrange,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = onVerifyClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerifyGreen)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check_circle),
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Verificar",
                            color = White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
            }
        }
    }
}

@Composable
private fun ModeratorBottomBarPending(
    selected: String,
    onNavigateToPanel: () -> Unit,
    onNavigateToPending: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    Surface(
        color = HeaderDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItemPending(
                label = "Panel",
                iconRes = R.drawable.ic_home,
                selected = selected == "panel",
                onClick = onNavigateToPanel
            )
            BottomBarItemPending(
                label = "Pendientes",
                iconRes = R.drawable.ic_schedule,
                selected = selected == "pendientes",
                onClick = onNavigateToPending
            )
            BottomBarItemPending(
                label = "Historial",
                iconRes = R.drawable.ic_library_books,
                selected = selected == "historial",
                onClick = onNavigateToHistory
            )
            BottomBarItemPending(
                label = "Cuenta",
                iconRes = R.drawable.ic_person,
                selected = selected == "cuenta",
                onClick = onNavigateToAccount
            )
        }
    }
}

@Composable
private fun BottomBarItemPending(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = if (selected) ActiveNavGreen else InactiveNav,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = if (selected) ActiveNavGreen else InactiveNav,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PendingPublicationsPreview() {
    RumboTheme {
        PendingPublicationsScreen(
            state = PendingPublicationsUiState(
                pendingCount = 8,
                publications = listOf(
                    Publication(
                        id = "1",
                        title = "Mirador de la Vereda Cruces",
                        description = "Hermoso mirador con vista panorámica",
                        category = Category.NATURE,
                        town = "Salento",
                        address = "Vereda Cruces",
                        schedule = "Abierto 24/7",
                        distanceKm = 5.2,
                        price = PriceLevel.FREE,
                        status = PublicationStatus.PENDING,
                        author = Author(id = "a1", name = "Mariana Ospina", levelName = "Exploradora"),
                        publishedAt = System.currentTimeMillis(),
                        votes = 0,
                        weeklyVotes = 0,
                        interested = false,
                        visited = false,
                        latitude = 4.638,
                        longitude = -75.570,
                        photos = emptyList(),
                        comments = emptyList()
                    )
                )
            )
        )
    }
}
