package com.app.rumbo.core.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import com.app.rumbo.R

/** Pestañas de la barra de navegación inferior. */
enum class RumboTab(@param:DrawableRes val icon: Int, @param:StringRes val labelRes: Int) {
    HOME(R.drawable.ic_home, R.string.tab_home),
    EXPLORE(R.drawable.ic_compass, R.string.tab_explore),
    CREATE(R.drawable.ic_add, R.string.tab_create),
    ALERTS(R.drawable.ic_notifications, R.string.tab_alerts),
    PROFILE(R.drawable.ic_person, R.string.tab_profile)
}

/**
 * Estructura común para las pantallas con barra inferior. Los insets superiores
 * los gestiona cada pantalla (top bar o [androidx.compose.foundation.layout.statusBarsPadding]);
 * aquí solo se reserva el espacio de la barra de navegación.
 */
@Composable
fun RumboScaffold(
    selectedTab: RumboTab,
    onTabSelected: (RumboTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = { RumboBottomBar(selectedTab, onTabSelected) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content
    )
}

@Composable
fun RumboBottomBar(selected: RumboTab, onTabSelected: (RumboTab) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(66.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RumboTab.entries.forEach { tab ->
                TabItem(tab, selected = tab == selected, onClick = { onTabSelected(tab) })
            }
        }
    }
}

@Composable
private fun TabItem(tab: RumboTab, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val label = stringResource(tab.labelRes)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(painterResource(tab.icon), contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}
