package com.app.rumbo.features.publicationdetail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.component.AuthorAvatar
import com.app.rumbo.core.component.PublicationPhoto
import com.app.rumbo.core.component.RumboTopBar
import com.app.rumbo.core.component.StatusChip
import com.app.rumbo.core.component.color
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.core.util.formatKm
import com.app.rumbo.core.util.formatThousands
import com.app.rumbo.core.util.toTimeAgo
import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Comment
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.usecase.MAX_COMMENT_LENGTH
import java.util.Locale

@Composable
fun PublicationDetailRoute(
    onBack: () -> Unit,
    viewModel: PublicationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val commentText by viewModel.commentText.collectAsStateWithLifecycle()
    PublicationDetailScreen(
        state = state,
        commentText = commentText,
        onBack = onBack,
        onToggleInterest = viewModel::onToggleInterest,
        onToggleVisited = viewModel::onToggleVisited,
        onCommentChange = viewModel::onCommentChange,
        onSendComment = viewModel::onSendComment,
        onToggleCommentLike = viewModel::onToggleCommentLike
    )
}

@Composable
fun PublicationDetailScreen(
    state: PublicationDetailUiState,
    commentText: String,
    onBack: () -> Unit,
    onToggleInterest: () -> Unit,
    onToggleVisited: () -> Unit,
    onCommentChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onToggleCommentLike: (String) -> Unit
) {
    Scaffold(topBar = { RumboTopBar("Detalle", onBack) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding()) {
            when (state) {
                PublicationDetailUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                PublicationDetailUiState.NotFound ->
                    Text(
                        "No encontramos esta publicación.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                is PublicationDetailUiState.Success ->
                    PublicationDetailContent(
                        state.publication, commentText, onToggleInterest, onToggleVisited, onCommentChange, onSendComment, onToggleCommentLike
                    )
            }
        }
    }
}

@Composable
private fun PublicationDetailContent(
    publication: Publication,
    commentText: String,
    onToggleInterest: () -> Unit,
    onToggleVisited: () -> Unit,
    onCommentChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onToggleCommentLike: (String) -> Unit
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Gallery(publication)

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Encabezado
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusChip(publication.status)
                    Text(
                        publication.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = publication.category.color
                    )
                }
                Text(publication.title, style = MaterialTheme.typography.headlineMedium)
            }

            if (publication.status == PublicationStatus.PENDING) PendingNotice()

            // Acciones principales
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onToggleInterest,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = if (publication.interested) {
                        ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF6E2), contentColor = Color(0xFF8A6A0B))
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_star), null, Modifier.size(18.dp))
                    Text(
                        "${if (publication.interested) "Te interesa" else "Me interesa"} · ${publication.votes.formatThousands()}",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                OutlinedButton(
                    onClick = onToggleVisited,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (publication.visited) Color(0xFF2B6B45) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        painterResource(if (publication.visited) R.drawable.ic_check_circle else R.drawable.ic_radio_unchecked),
                        null,
                        Modifier.size(18.dp)
                    )
                    Text(if (publication.visited) "Visitado" else "No visitado", modifier = Modifier.padding(start = 6.dp))
                }
            }

            Text(publication.description, style = MaterialTheme.typography.bodyLarge)

            // Datos del lugar
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(R.drawable.ic_place, "Dirección", "${publication.address} · ${publication.town}")
                InfoRow(R.drawable.ic_schedule, "Horario", publication.schedule)
                InfoRow(R.drawable.ic_map, "Distancia", "${publication.distanceKm.formatKm()} de tu ubicación")
                InfoRow(R.drawable.ic_money, "Precio", publication.price.label)
            }

            MapPreview(publication)

            // Autor
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthorAvatar(publication.author, size = 44.dp)
                Column {
                    Text("Publicado por ${publication.author.name}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${publication.author.levelName} · ${publication.publishedAt.toTimeAgo()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            CommentsSection(publication, commentText, onCommentChange, onSendComment, onToggleCommentLike)
        }
    }
}

@Composable
private fun Gallery(publication: Publication) {
    val photos = publication.photos
    // Sin fotos se muestra una sola página con el marcador de posición
    val pagerState = rememberPagerState { photos.size.coerceAtLeast(1) }
    Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
        HorizontalPager(state = pagerState) { page ->
            PublicationPhoto(
                photo = photos.getOrNull(page),
                fallbackLabel = publication.title,
                tint = publication.category.color,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (photos.size > 1) {
            Text(
                "${pagerState.currentPage + 1} / ${photos.size}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color(0x99000000), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun PendingNotice() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFFDF3DC), MaterialTheme.shapes.medium)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(painterResource(R.drawable.ic_schedule), null, Modifier.size(18.dp), tint = Color(0xFF8A6A0B))
        Text(
            "Un moderador aún no ha verificado esta publicación. La información podría cambiar.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8A6A0B)
        )
    }
}

@Composable
private fun InfoRow(@DrawableRes icon: Int, label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(painterResource(icon), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Vista previa del mapa: maqueta hasta integrar Google Maps o Mapbox. */
@Composable
private fun MapPreview(publication: Publication) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color(0xFFE4EEE9), MaterialTheme.shapes.large),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(R.drawable.ic_place), null, Modifier.size(36.dp), tint = publication.category.color)
        Text(
            String.format(Locale.US, "%.4f, %.4f", publication.latitude, publication.longitude),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        )
    }
}

@Composable
private fun CommentsSection(
    publication: Publication,
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onToggleCommentLike: (String) -> Unit
) {
    val canInteract = publication.status == PublicationStatus.VERIFIED
    val comments = publication.comments
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Comentarios (${comments.size})", style = MaterialTheme.typography.titleMedium)
        if (comments.isEmpty()) {
            Text(
                "Todavía no hay comentarios. ¡Sé el primero en contar tu experiencia!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        comments.forEach { comment ->
            CommentItem(comment, canInteract, onLikeClick = { onToggleCommentLike(comment.id) })
        }
        if (publication.status == PublicationStatus.VERIFIED) {
            CommentInput(commentText, onCommentChange, onSendComment)
        } else {
            Text(
                if (publication.status == PublicationStatus.PENDING) {
                    "Los comentarios se habilitan cuando un moderador verifique la publicación."
                } else {
                    "Esta publicación ya no recibe comentarios."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommentInput(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Cuenta tu experiencia") },
        minLines = 2,
        shape = MaterialTheme.shapes.medium,
        supportingText = {
            Text(
                "${text.length} / $MAX_COMMENT_LENGTH",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        },
        trailingIcon = {
            IconButton(onClick = onSend, enabled = text.isNotBlank()) {
                Icon(painterResource(R.drawable.ic_send), contentDescription = "Enviar comentario")
            }
        }
    )
}

@Composable
private fun CommentItem(comment: Comment, canLike: Boolean, onLikeClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AuthorAvatar(comment.author, size = 32.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${comment.author.name} · ${comment.author.levelName} · ${comment.publishedAt.toTimeAgo()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
            val likeColor = if (comment.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        enabled = canLike,
                        role = Role.Button,
                        onClickLabel = if (comment.liked) "Quitar me gusta" else "Me gusta",
                        onClick = onLikeClick
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(painterResource(R.drawable.ic_thumb_up), null, Modifier.size(14.dp), tint = likeColor)
                Text(comment.likes.toString(), style = MaterialTheme.typography.labelMedium, color = likeColor)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun PublicationDetailScreenPreview() {
    RumboTheme {
        PublicationDetailScreen(
            state = PublicationDetailUiState.Success(SamplePublications.create().first()),
            commentText = "",
            onBack = {}, onToggleInterest = {}, onToggleVisited = {}, onCommentChange = {}, onSendComment = {},
            onToggleCommentLike = {}
        )
    }
}
