package com.app.rumbo.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.app.rumbo.R

/**
 * Marcador de posición para fotos, como en el prototipo. Se reemplazará por la
 * imagen real cuando las publicaciones tengan fotografías.
 */
@Composable
fun PhotoPlaceholder(label: String, tint: Color, modifier: Modifier = Modifier) {
    Box(modifier.background(tint.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(painterResource(R.drawable.ic_photo), contentDescription = null, tint = tint.copy(alpha = 0.6f), modifier = Modifier.size(28.dp))
            Text(
                "foto · $label",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = tint
            )
        }
    }
}
