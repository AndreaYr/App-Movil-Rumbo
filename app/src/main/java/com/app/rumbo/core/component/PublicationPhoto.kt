package com.app.rumbo.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.app.rumbo.domain.model.Photo

/**
 * Foto remota de una publicación. Mientras descarga, si falla (p. ej. sin conexión)
 * o si la publicación no tiene fotos, muestra el marcador de posición.
 */
@Composable
fun PublicationPhoto(
    photo: Photo?,
    fallbackLabel: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        if (photo == null) {
            PhotoPlaceholder(fallbackLabel, tint, Modifier.matchParentSize())
            return@Box
        }
        SubcomposeAsyncImage(
            model = photo.url,
            contentDescription = photo.description,
            contentScale = ContentScale.Crop,
            loading = { PhotoPlaceholder(photo.description, tint, Modifier.fillMaxSize()) },
            error = { PhotoPlaceholder(photo.description, tint, Modifier.fillMaxSize()) },
            modifier = Modifier.matchParentSize()
        )
        // Crédito del autor: requisito de las licencias Creative Commons
        photo.credit?.let { credit ->
            Text(
                "Foto: $credit",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.sp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color(0x80000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
