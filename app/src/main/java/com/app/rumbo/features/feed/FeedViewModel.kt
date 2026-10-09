package com.app.rumbo.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.FeedSort
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.usecase.GetFeedUseCase
import com.app.rumbo.domain.usecase.ToggleInterestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = true,
    val publications: List<Publication> = emptyList(),
    val selectedCategory: Category? = null,
    val sort: FeedSort = FeedSort.RECENT
)

private data class FeedFilters(val category: Category? = null, val sort: FeedSort = FeedSort.RECENT)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeed: GetFeedUseCase,
    private val toggleInterest: ToggleInterestUseCase
) : ViewModel() {

    private val filters = MutableStateFlow(FeedFilters())

    // Cada vez que cambia un filtro se vuelve a consultar el feed con los nuevos criterios
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FeedUiState> = filters
        .flatMapLatest { f ->
            getFeed(f.category, f.sort).map { publications ->
                FeedUiState(
                    isLoading = false,
                    publications = publications,
                    selectedCategory = f.category,
                    sort = f.sort
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState())

    /** Tocar la categoría activa la desactiva y vuelve a mostrar todas. */
    fun onCategorySelected(category: Category?) {
        filters.update { it.copy(category = if (it.category == category) null else category) }
    }

    fun onSortSelected(sort: FeedSort) {
        filters.update { it.copy(sort = sort) }
    }

    fun onToggleInterest(publicationId: String) {
        viewModelScope.launch { toggleInterest(publicationId) }
    }
}
