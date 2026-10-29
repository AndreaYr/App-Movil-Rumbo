package com.app.rumbo.core.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.rumbo.R

enum class ModeratorTab(@param:DrawableRes val icon: Int, val label: String) {
    PANEL(R.drawable.ic_home, "Panel"),
    PENDING(R.drawable.ic_schedule, "Pendientes"),
    HISTORY(R.drawable.ic_library_books, "Historial"),
    ACCOUNT(R.drawable.ic_person, "Cuenta")
}

private val BarBackground = Color(0xFF0F1E1B)
private val ActiveNavGreen = Color(0xFF23C49D)
private val InactiveNav = Color(0xFF6B7C75)

@Composable
fun ModeratorBottomBar(
    selectedTab: ModeratorTab,
    onPanelClick: () -> Unit,
    onPendingClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BarBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeratorBottomBarItem(
                tab = ModeratorTab.PANEL,
                selected = selectedTab == ModeratorTab.PANEL,
                onClick = onPanelClick
            )
            ModeratorBottomBarItem(
                tab = ModeratorTab.PENDING,
                selected = selectedTab == ModeratorTab.PENDING,
                onClick = onPendingClick
            )
            ModeratorBottomBarItem(
                tab = ModeratorTab.HISTORY,
                selected = selectedTab == ModeratorTab.HISTORY,
                onClick = onHistoryClick
            )
            ModeratorBottomBarItem(
                tab = ModeratorTab.ACCOUNT,
                selected = selectedTab == ModeratorTab.ACCOUNT,
                onClick = onAccountClick
            )
        }
    }
}

@Composable
private fun ModeratorBottomBarItem(
    tab: ModeratorTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) ActiveNavGreen else InactiveNav
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(tab.icon),
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = tab.label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
