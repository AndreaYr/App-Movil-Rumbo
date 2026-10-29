package com.app.rumbo.features.moderatordashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.rumbo.R
import com.app.rumbo.core.component.ModeratorBottomBar
import com.app.rumbo.core.component.ModeratorTab
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.domain.model.ModerationActionType
import com.app.rumbo.domain.model.ModerationActivity
import com.app.rumbo.domain.model.ModeratorStats

private val HeaderDark = Color(0xFF0F1E1B)
private val BodyBeige = Color(0xFFF7F5EE)
private val OrangeCta = Color(0xFFF05A36)
private val Terracotta = Color(0xFFC85B38)
private val EmeraldGreen = Color(0xFF117361)
private val PendingYellow = Color(0xFFF2C94C)
private val RejectedOrange = Color(0xFFE76F51)
private val FinishedGray = Color(0xFFBDBDBD)
private val TextDark = Color(0xFF0F1E1B)
private val TextSecondary = Color(0xFF6B7C75)

@Composable
fun ModeratorDashboardScreen(
    state: ModeratorDashboardUiState,
    onStartReview: () -> Unit = {},
    onNavigateToPending: () -> Unit = {},
    onNavigateToVerified: () -> Unit = {},
    onNavigateToRejected: () -> Unit = {},
    onNavigateToFinished: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        containerColor = BodyBeige,
        bottomBar = {
            ModeratorBottomBar(
                selectedTab = ModeratorTab.PANEL,
                onPanelClick = {},
                onPendingClick = onNavigateToPending,
                onHistoryClick = onNavigateToHistory,
                onAccountClick = onNavigateToAccount
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = HeaderDark,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 40.dp, bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F3B35)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "LG",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.userName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = state.userRole,
                                    color = Terracotta,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        IconButton(onClick = onLogout) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF182B27)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_forward),
                                    contentDescription = "Cerrar sesión",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "${state.stats.pending} publicaciones esperan revisión",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 30.sp
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = onStartReview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeCta)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Empezar a revisar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // KPI Grid 2x2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).clickable { onNavigateToPending() },
                        label = "PENDIENTES",
                        value = state.stats.pending.toString(),
                        subtext = if (state.stats.pending > 0) "2 con más de 3 días" else "Sin pendientes",
                        iconColor = PendingYellow
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).clickable { onNavigateToVerified() },
                        label = "VERIFICADAS",
                        value = state.stats.verified.toString(),
                        subtext = "+14 esta semana",
                        iconColor = EmeraldGreen
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).clickable { onNavigateToRejected() },
                        label = "RECHAZADAS",
                        value = state.stats.rejected.toString(),
                        subtext = "motivo más común: fotos",
                        iconColor = RejectedOrange
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).clickable { onNavigateToFinished() },
                        label = "FINALIZADAS",
                        value = state.stats.finished.toString(),
                        subtext = "eventos y temporales",
                        iconColor = FinishedGray
                    )
                }
                
                // Total publicaciones
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total de publicaciones",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = state.stats.total.toString(),
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                
                // Desglose por estado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Desglose por estado",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        // Barra segmentada
                        if (state.stats.total > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                if (state.stats.verified > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(state.stats.verified.toFloat())
                                            .fillMaxSize()
                                            .background(EmeraldGreen)
                                    )
                                }
                                if (state.stats.pending > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(state.stats.pending.toFloat())
                                            .fillMaxSize()
                                            .background(PendingYellow)
                                    )
                                }
                                if (state.stats.rejected > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(state.stats.rejected.toFloat())
                                            .fillMaxSize()
                                            .background(RejectedOrange)
                                    )
                                }
                                if (state.stats.finished > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(state.stats.finished.toFloat())
                                            .fillMaxSize()
                                            .background(FinishedGray)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LegendItem(color = EmeraldGreen, label = "Verificadas", count = state.stats.verified)
                                    LegendItem(color = PendingYellow, label = "Pendientes", count = state.stats.pending)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LegendItem(color = RejectedOrange, label = "Rechazadas", count = state.stats.rejected)
                                    LegendItem(color = FinishedGray, label = "Finalizadas", count = state.stats.finished)
                                }
                            }
                        } else {
                            Text(
                                text = "Sin publicaciones",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                // Actividad reciente
                Text(
                    text = "Actividad reciente",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                state.recentActivity.forEach { activity ->
                    ActivityItem(activity = activity)
                }
                
                if (state.recentActivity.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Sin actividad reciente",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(iconColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtext,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $count",
            color = Color(0xFF4F4F4F),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActivityItem(activity: ModerationActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (activity.actionType) {
                            ModerationActionType.VERIFIED -> Color(0xFFE8F5F1)
                            ModerationActionType.REJECTED -> Color(0xFFFDECEA)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (activity.actionType == ModerationActionType.VERIFIED) R.drawable.ic_check_circle else R.drawable.ic_cancel),
                    contentDescription = null,
                    tint = if (activity.actionType == ModerationActionType.VERIFIED) EmeraldGreen else RejectedOrange,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (activity.actionType == ModerationActionType.VERIFIED) {
                        "Verificaste \"${activity.publicationTitle.replace("Verificaste ", "").replace("Rechazaste ", "")}\""
                    } else {
                        "Rechazaste \"${activity.publicationTitle.replace("Verificaste ", "").replace("Rechazaste ", "")}\" · Fotografías no válidas"
                    },
                    color = TextDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${activity.timeAgo} · ${activity.authorName}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModeratorDashboardPreview() {
    RumboTheme {
        ModeratorDashboardScreen(
            state = ModeratorDashboardUiState(
                stats = ModeratorStats(pending = 8, verified = 259, rejected = 37, finished = 66, total = 412),
                recentActivity = listOf(
                    ModerationActivity(
                        id = "1",
                        actionType = ModerationActionType.VERIFIED,
                        publicationTitle = "Verificaste Mirador Alto de la Cruz",
                        authorName = "Mariana Ospina",
                        timeAgo = "hace 25 min"
                    ),
                    ModerationActivity(
                        id = "2",
                        actionType = ModerationActionType.REJECTED,
                        publicationTitle = "Rechazaste Bar La Esquina",
                        authorName = "Carlos Pérez",
                        timeAgo = "hace 1 h"
                    )
                )
            )
        )
    }
}
