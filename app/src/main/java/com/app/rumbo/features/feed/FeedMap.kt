package com.app.rumbo.features.feed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.rumbo.R
import com.app.rumbo.core.util.label
import com.app.rumbo.core.component.MapBox
import com.app.rumbo.core.component.PublicationPhoto
import com.app.rumbo.core.component.color
import com.app.rumbo.core.component.toMapMarker
import com.app.rumbo.core.util.formatKm
import com.app.rumbo.core.util.formatThousands
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Publication

/** Vista de mapa de Explorar: marcadores por categoría, búsqueda y tarjeta del lugar seleccionado. */
@Composable
internal fun FeedMap(
    state: FeedUiState,
    onCategorySelected: (Category?) -> Unit,
    onPublicationClick: (String) -> Unit,
    onShowList: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val publications = remember(state.publications, query) {
        val text = query.trim()
        state.publications.filter {
            it.title.contains(text, ignoreCase = true) || it.town.contains(text, ignoreCase = true)
        }
    }
    val markers = remember(publications) { publications.map { it.toMapMarker() } }
    val selected = publications.firstOrNull { it.id == selectedId }

    Box(modifier.fillMaxSize()) {
        MapBox(
            modifier = Modifier.fillMaxSize(),
            zoom = 9.5,
            markers = markers,
            myLocationButtonAlignment = Alignment.CenterEnd,
            onMarkerClick = { selectedId = it.id },
            onMapClick = { selectedId = null }
        )
        Column(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapSearchField(query, { query = it }, Modifier.weight(1f))
                FeedViewToggle(showMap = true, onChange = { if (!it) onShowList() })
            }
            CategoryFilters(state.selectedCategory, onCategorySelected)
        }
        selected?.let { publication ->
            // El margen inferior deja visibles el logo y la atribución de Mapbox
            MapPublicationCard(
                publication = publication,
                onClick = { onPublicationClick(publication.id) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 36.dp)
            )
        }
    }
}

@Composable
private fun MapSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(painterResource(R.drawable.ic_search), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f),
            decorationBox = { field ->
                if (query.isEmpty()) {
                    Text(
                        stringResource(R.string.feed_map_search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                field()
            }
        )
    }
}

/** Conmutador Lista/Mapa de Explorar. */
@Composable
internal fun FeedViewToggle(showMap: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(3.dp)
    ) {
        ToggleOption(R.drawable.ic_library_books, stringResource(R.string.feed_view_list), selected = !showMap) { onChange(false) }
        ToggleOption(R.drawable.ic_map, stringResource(R.string.feed_view_map), selected = showMap) { onChange(true) }
    }
}

@Composable
private fun ToggleOption(@DrawableRes icon: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp)
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(painterResource(icon), null, Modifier.size(15.dp), tint = tint)
        Text(label, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

@Composable
private fun MapPublicationCard(publication: Publication, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PublicationPhoto(
            photo = publication.photos.firstOrNull(),
            fallbackLabel = publication.title,
            tint = publication.category.color,
            modifier = Modifier.size(76.dp).clip(MaterialTheme.shapes.medium)
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                publication.category.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = publication.category.color
            )
            Text(publication.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(R.string.feed_map_info_line, publication.town, publication.distanceKm.formatKm(), publication.price.label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(painterResource(R.drawable.ic_star), null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                Text(publication.votes.formatThousands(), style = MaterialTheme.typography.labelMedium)
                Text(
                    stringResource(R.string.feed_view_detail),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    maxLines = 1
                )
                Icon(painterResource(R.drawable.ic_arrow_forward), null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
