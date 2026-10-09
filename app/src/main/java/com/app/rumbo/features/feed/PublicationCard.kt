package com.app.rumbo.features.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.rumbo.R
import com.app.rumbo.core.component.AuthorAvatar
import com.app.rumbo.core.component.PublicationPhoto
import com.app.rumbo.core.component.StatusChip
import com.app.rumbo.core.component.color
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.formatKm
import com.app.rumbo.core.util.formatThousands
import com.app.rumbo.core.util.toTimeAgo
import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Publication

/** Tarjeta del feed: autor, foto con estado, datos del lugar y acciones. */
@Composable
fun PublicationCard(
    publication: Publication,
    onClick: () -> Unit,
    onToggleInterest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
    ) {
        // Autor
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuthorAvatar(publication.author)
            Column(Modifier.weight(1f)) {
                Text(publication.author.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${publication.author.levelName} · ${publication.publishedAt.toTimeAgo()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Foto (4:3) con el estado sobrepuesto
        Box {
            PublicationPhoto(
                photo = publication.photos.firstOrNull(),
                fallbackLabel = publication.title,
                tint = publication.category.color,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)
            )
            StatusChip(publication.status, Modifier.align(Alignment.TopStart).padding(12.dp))
        }

        // Datos del lugar
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                publication.category.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = publication.category.color
            )
            Text(
                publication.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(painterResource(R.drawable.ic_place), null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${publication.town} · ${publication.distanceKm.formatKm()} · ${publication.price.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Acciones: voto, comentarios y visitado
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                VotePill(publication.votes, publication.interested, onToggleInterest)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painterResource(R.drawable.ic_chat_bubble), null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(publication.comments.size.toString(), style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.weight(1f))
                VisitedLabel(publication.visited)
            }
        }
    }
}

@Composable
private fun VotePill(votes: Int, interested: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    val background = if (interested) Color(0xFFFEF6E2) else MaterialTheme.colorScheme.surface
    val border = if (interested) Color(0xFFF0DCA8) else Color(0xFFE9E3D8)
    val content = if (interested) Color(0xFF8A6A0B) else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(role = Role.Button, onClickLabel = if (interested) "Quitar interés" else "Me interesa", onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(painterResource(R.drawable.ic_star), null, Modifier.size(16.dp), tint = if (interested) MaterialTheme.colorScheme.tertiary else content)
        Text(votes.formatThousands(), style = MaterialTheme.typography.labelMedium, color = content)
    }
}

@Composable
private fun VisitedLabel(visited: Boolean) {
    val color = if (visited) Color(0xFF2B6B45) else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            painterResource(if (visited) R.drawable.ic_check_circle else R.drawable.ic_radio_unchecked),
            null,
            Modifier.size(16.dp),
            tint = color
        )
        Text(if (visited) "Visitado" else "No visitado", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicationCardPreview() {
    RumboTheme {
        PublicationCard(SamplePublications.create().first(), onClick = {}, onToggleInterest = {}, modifier = Modifier.padding(16.dp))
    }
}
