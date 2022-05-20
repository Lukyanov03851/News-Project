package com.news.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.news.R
import com.news.data.NewsRepo
import com.news.data.SourcesRepo
import com.news.model.domain.NewsModel
import com.news.model.domain.SortByModel
import com.news.model.domain.SourceModel
import com.news.model.dto.SortBy
import com.news.network.Status
import com.news.utils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NewsListViewModel @Inject constructor(
    newsRepo: NewsRepo,
    private val sourcesRepo: SourcesRepo
) : ViewModel() {

    companion object {
        const val TAG = "NewsListViewModel"
        const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private var sortMode: SortBy = SortBy.POPULARITY
    private var source: String? = null
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    private val _uiState = MutableStateFlow(NewsListUiState())
    val uiState: StateFlow<NewsListUiState> = _uiState.asStateFlow()

    val pagingDataFlow: Flow<PagingData<NewsModel>>

    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val sortMode = actionStateFlow
            .filterIsInstance<UiAction.UpdateList>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.UpdateList(sortBy = sortMode, source = source, period = Pair(startDate, endDate))) }

        pagingDataFlow = sortMode
            .flatMapLatest { newsRepo.getNewsResultStream(
                sortBy = it.sortBy,
                source = it.source,
                from = formatDate(startDate, DATE_FORMAT),
                to = formatDate(endDate, DATE_FORMAT)
            ) }
            .cachedIn(viewModelScope)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    fun onSortOptionClicked() {
        val sortOptionsList = listOf(
            SortByModel(
                SortBy.POPULARITY,
                R.string.option_popularity,
                sortMode == SortBy.POPULARITY
            ),
            SortByModel(
                SortBy.PUBLISHED_AT,
                R.string.option_published_at,
                sortMode == SortBy.PUBLISHED_AT
            )
        )

        _uiState.update { it.copy(showSortOptionsMenu = sortOptionsList) }
    }

    fun selectSortMode(mode: SortBy) {
        sortMode = mode
        updateListParams()
    }

    fun onFilterBySourceOptionClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDataLoading = true) }
            val resource = sourcesRepo.getSourcesList()
            if (resource.status == Status.SUCCESS && resource.data != null) {
                resource.data.firstOrNull { it.id == source }?.isSelected = true
                _uiState.update { it.copy(showSourcesListMenu = resource.data) }
            } else {
                _uiState.update { it.copy(errorMsg = resource.message) }
            }
            _uiState.update { it.copy(isDataLoading = false) }
        }
    }

    fun selectSource(value: SourceModel) {
        source = value.id
        updateListParams()
    }

    fun onFilterByDateClicked() {
        _uiState.update {
            it.copy(
                showDatePicker = Pair(
                    startDate ?: Calendar.getInstance(),
                    endDate ?: Calendar.getInstance()
                )
            )
        }
    }

    fun selectPeriod(start: Calendar, end: Calendar) {
        startDate = start
        endDate = end
        updateListParams()
    }

    private fun updateListParams(){
        accept(UiAction.UpdateList(sortBy = sortMode, source = source, period = Pair(startDate, endDate)))
    }

    fun onErrorMsgShown() {
        _uiState.update {
            it.copy(errorMsg = null)
        }
    }

    fun onSortOptionsMenuShown() {
        _uiState.update {
            it.copy(showSortOptionsMenu = null)
        }
    }

    fun onSourcesListMenuShown() {
        _uiState.update {
            it.copy(showSourcesListMenu = null)
        }
    }

    fun onDatePickerShown() {
        _uiState.update {
            it.copy(showDatePicker = null)
        }
    }

    fun setListLoadingState(value: Boolean) {
        _uiState.update {
            it.copy(isListLoading = value)
        }
    }

}

data class NewsListUiState(
    val isListLoading: Boolean = false,
    val isDataLoading: Boolean = false,
    val errorMsg: String? = null,
    val showSortOptionsMenu: List<SortByModel>? = null,
    val showSourcesListMenu: List<SourceModel>? = null,
    val showDatePicker: Pair<Calendar, Calendar>? = null
)

sealed class UiAction {
    data class UpdateList(
        val sortBy: SortBy,
        val source: String?,
        val period: Pair<Calendar?, Calendar?>
    ) : UiAction()
}