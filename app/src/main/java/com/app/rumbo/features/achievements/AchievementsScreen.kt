package com.app.rumbo.features.achievements

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.util.description
import com.app.rumbo.core.util.displayName
import com.app.rumbo.core.component.iconRes
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.domain.model.Achievement
import com.app.rumbo.domain.model.Badge

private val GoldDark: Color @Composable get() = adaptiveColor(Color(0xFF6B5510), Color(0xFFE3C26A))
private val GoldMid: Color @Composable get() = adaptiveColor(Color(0xFFB4841A), DarkGoldText)
private val GoldBorder: Color @Composable get() = adaptiveColor(Color(0xFFEFDFB4), Color(0xFF5A4A1E))
private val LockedBackground: Color @Composable get() = adaptiveColor(Color(0xFFF6F2EA), Color(0xFF1F2A27))
private val LockedBorder: Color @Composable get() = adaptiveColor(Color(0xFFD8D1C4), Color(0xFF3A4642))
private val LockedChip: Color @Composable get() = adaptiveColor(Color(0xFFE9E4D9), Color(0xFF2E3B38))
private val LockedText: Color @Composable get() = adaptiveColor(Color(0xFF4A5652), Color(0xFFB7C2BE))

@Composable
fun AchievementsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AchievementsScreen(state = state, onBack = onBack, modifier = modifier)
}

@Composable
fun AchievementsScreen(
    state: AchievementsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        TopRow(onBack, Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp))
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { SummaryCard(state) }
            if (state.unlocked.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionLabel(stringResource(R.string.achievements_section_unlocked, state.unlocked.size), Modifier.padding(top = 12.dp))
                }
                items(state.unlocked, key = { it.achievement.name }) { UnlockedCard(it) }
            }
            if (state.locked.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionLabel(stringResource(R.string.achievements_section_locked, state.locked.size), Modifier.padding(top = 14.dp))
                }
                items(state.locked, key = { it.achievement.name }, span = { GridItemSpan(maxLineSpan) }) { LockedCard(it) }
            }
        }
    }
}

@Composable
private fun TopRow(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.common_back), onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_arrow_back), null, Modifier.size(20.dp))
        }
        Text(stringResource(R.string.achievements_title), style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
    }
}

@Composable
private fun SummaryCard(state: AchievementsUiState) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(adaptiveColor(Color(0xFFFDF7E7), Color(0xFF3A3010)), shape)
            .border(1.dp, GoldBorder, shape)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp).background(Color(0xFFF2C94C), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_military_tech), null, Modifier.size(28.dp), tint = GoldDark)
        }
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.achievements_summary, state.unlocked.size, state.total),
                style = MaterialTheme.typography.titleSmall,
                color = GoldDark
            )
            ProgressBar(
                progress = state.unlockedProgress,
                track = GoldDark.copy(alpha = 0.15f),
                fill = GoldMid,
                height = 7.dp,
                modifier = Modifier.padding(top = 9.dp)
            )
            Text(
                stringResource(R.string.achievements_points, state.pointsEarned),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
                color = adaptiveColor(Color(0xFF8A6A0B), Color(0xFFE3C26A)),
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = LockedText, modifier = modifier)
}

@Composable
private fun UnlockedCard(badge: Badge) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.large)
                .border(1.5.dp, GoldBorder, MaterialTheme.shapes.large),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(badge.achievement.iconRes), null, Modifier.size(27.dp), tint = GoldMid)
        }
        Text(
            badge.achievement.displayName,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            stringResource(R.string.achievements_unlocked),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 5.dp)
        )
        Text(
            stringResource(R.string.achievements_points_badge, badge.achievement.points),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp),
            color = GoldMid,
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}

@Composable
private fun LockedCard(badge: Badge) {
    val shape = MaterialTheme.shapes.medium
    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
    val lockedBorder = LockedBorder
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LockedBackground, shape)
            .drawBehind {
                drawRoundRect(
                    color = lockedBorder,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = dash)
                )
            }
            .padding(13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(46.dp).background(LockedChip, shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_lock), null, Modifier.size(22.dp), tint = Color(0xFF9AA29E))
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    badge.achievement.displayName,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                    color = LockedText,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    stringResource(R.string.achievements_locked_badge),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.background(LockedChip, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                badge.achievement.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgressBar(
                    progress = badge.progress,
                    track = adaptiveColor(Color(0xFFE2DCCF), Color(0xFF2E3B38)),
                    fill = MaterialTheme.colorScheme.primary,
                    height = 6.dp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${badge.current.coerceAtMost(badge.achievement.target)}/${badge.achievement.target}",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = accentTeal()
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, track: Color, fill: Color, height: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(height).background(track, CircleShape)) {
        Box(Modifier.fillMaxWidth(progress).height(height).background(fill, CircleShape))
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AchievementsScreenPreview() {
    RumboTheme {
        AchievementsScreen(
            state = AchievementsUiState(
                isLoading = false,
                unlocked = listOf(
                    Badge(Achievement.FIRST_PUBLICATION, 3),
                    Badge(Achievement.FIRST_VISIT, 12),
                    Badge(Achievement.WALKER, 12)
                ),
                locked = listOf(
                    Badge(Achievement.TEN_VERIFIED, 6),
                    Badge(Achievement.FIFTY_VOTES, 31),
                    Badge(Achievement.CITY_EXPLORER, 28),
                    Badge(Achievement.MONTH_HIGHLIGHT, 0)
                ),
                pointsEarned = 75
            ),
            onBack = {}
        )
    }
}
