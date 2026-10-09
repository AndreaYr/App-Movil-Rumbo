package com.app.rumbo.features.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.R
import com.app.rumbo.core.theme.RumboTheme
import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.FeedSort

@Composable
fun FeedRoute(
    onPublicationClick: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeedScreen(
        state = state,
        onCategorySelected = viewModel::onCategorySelected,
        onSortSelected = viewModel::onSortSelected,
        onPublicationClick = onPublicationClick,
        onToggleInterest = viewModel::onToggleInterest
    )
}

@Composable
fun FeedScreen(
    state: FeedUiState,
    onCategorySelected: (Category?) -> Unit,
    onSortSelected: (FeedSort) -> Unit,
    onPublicationClick: (String) -> Unit,
    onToggleInterest: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Explorar", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Lugares recomendados por la comunidad del Quindío",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            CategoryFilters(state.selectedCategory, onCategorySelected)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.isLoading) "" else "${state.publications.size} publicaciones",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                SortMenu(state.sort, onSortSelected)
            }
        }
        when {
            state.isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.publications.isEmpty() -> item { EmptyFeed() }
            else -> items(state.publications, key = { it.id }) { publication ->
                PublicationCard(
                    publication = publication,
                    onClick = { onPublicationClick(publication.id) },
                    onToggleInterest = { onToggleInterest(publication.id) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryFilters(selected: Category?, onSelected: (Category?) -> Unit) {
    // Selección en verde primario, como en el prototipo
    val colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelected(null) }, label = { Text("Todas") }, colors = colors)
        }
        items(Category.entries) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(category) },
                label = { Text(category.label) },
                colors = colors
            )
        }
    }
}

@Composable
private fun SortMenu(current: FeedSort, onSelected: (FeedSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(current.label) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_sort), contentDescription = "Ordenar", modifier = Modifier.size(18.dp)) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FeedSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = { Text(sort.label) },
                    onClick = {
                        onSelected(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyFeed() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painterResource(R.drawable.ic_place),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )
        Text("Aún no hay publicaciones", style = MaterialTheme.typography.titleMedium)
        Text(
            "Prueba con otra categoría o sé el primero en compartir un lugar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FeedScreenPreview() {
    RumboTheme {
        FeedScreen(
            state = FeedUiState(isLoading = false, publications = SamplePublications.create().take(3)),
            onCategorySelected = {}, onSortSelected = {}, onPublicationClick = {}, onToggleInterest = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenEmptyPreview() {
    RumboTheme {
        FeedScreen(
            state = FeedUiState(isLoading = false, selectedCategory = Category.HISTORY),
            onCategorySelected = {}, onSortSelected = {}, onPublicationClick = {}, onToggleInterest = {}
        )
    }
}
