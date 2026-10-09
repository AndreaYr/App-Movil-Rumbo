package com.app.rumbo.core.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.rumbo.R
import com.app.rumbo.domain.model.PublicationStatus

private data class StatusStyle(
    val background: Color,
    val border: Color,
    val content: Color,
    @param:DrawableRes val icon: Int
)

private val PublicationStatus.style: StatusStyle
    get() = when (this) {
        PublicationStatus.PENDING -> StatusStyle(Color(0xFFFDF3DC), Color(0xFFEFDFB4), Color(0xFF8A6A0B), R.drawable.ic_schedule)
        PublicationStatus.VERIFIED -> StatusStyle(Color(0xFFE2F1EC), Color(0xFFBEDDD2), Color(0xFF0A4E45), R.drawable.ic_check_circle)
        PublicationStatus.REJECTED -> StatusStyle(Color(0xFFFCE8E2), Color(0xFFF0C4B6), Color(0xFF8E2F13), R.drawable.ic_cancel)
        PublicationStatus.FINISHED -> StatusStyle(Color(0xFFECEEF1), Color(0xFFD8DDE1), Color(0xFF4A5458), R.drawable.ic_task_alt)
    }

/** Estado de una publicación: icono + texto + color, para que funcione también con daltonismo. */
@Composable
fun StatusChip(status: PublicationStatus, modifier: Modifier = Modifier) {
    val style = status.style
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .background(style.background, shape)
            .border(1.dp, style.border, shape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(painterResource(style.icon), contentDescription = null, tint = style.content, modifier = Modifier.size(13.dp))
        Text(status.label, style = MaterialTheme.typography.labelMedium, color = style.content)
    }
}
