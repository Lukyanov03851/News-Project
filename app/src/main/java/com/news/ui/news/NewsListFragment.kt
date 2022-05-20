package com.news.ui.news

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import com.news.R
import com.news.adapters.*
import com.news.databinding.FragmentNewsListBinding
import com.news.model.domain.NewsModel
import com.news.model.domain.SortByModel
import com.news.model.domain.SourceModel
import com.news.utils.collectIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class NewsListFragment : Fragment() {

    companion object {
        const val TAG = "NewsListFragment"
    }

    private var mBinding: FragmentNewsListBinding? = null
    private val viewModel: NewsListViewModel by viewModels()
    private var pagingAdapter: NewsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewsListBinding.inflate(inflater, container, false)
        context ?: return binding.root
        mBinding = binding
        subscribeUi()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupPoolToRefresh()
        mBinding?.btnTryAgain?.setOnClickListener {
            reloadData()
        }
    }

    private fun setupPoolToRefresh() {
        mBinding?.refreshContainer?.setOnRefreshListener {
            reloadData()
            mBinding?.refreshContainer?.isRefreshing = false
        }
    }

    private fun setupList() {
        pagingAdapter = NewsAdapter(object : NewsAdapterCallback {
            override fun onAddToFavoriteClicked(item: NewsModel) {
                TODO("Not yet implemented")
            }

            override fun onShareBtnClicked(item: NewsModel) {
                TODO("Not yet implemented")
            }

            override fun onSaveImageClicked(item: NewsModel) {
                TODO("Not yet implemented")
            }
        })

        mBinding?.rvList?.adapter = pagingAdapter?.withLoadStateFooter(
            NewsLoadStateAdapter { pagingAdapter?.retry() }
        )

        lifecycleScope.launch {
            viewModel.pagingDataFlow.collectLatest { pagingAdapter?.submitData(it) }
        }

        lifecycleScope.launch {
            pagingAdapter?.loadStateFlow?.collect { loadState ->
                viewModel.setListLoadingState(loadState.source.refresh is LoadState.Loading)
                mBinding?.refreshContainer?.isVisible = loadState.source.refresh !is LoadState.Error
                mBinding?.errorContainer?.isVisible = loadState.source.refresh is LoadState.Error
            }
        }
    }

    private fun subscribeUi() {
        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.uiState.collectIn(viewLifecycleOwner) { uiState ->
                mBinding?.progressLoading?.root?.isVisible =
                    uiState.isDataLoading || uiState.isListLoading

                uiState.showSortOptionsMenu?.let {
                    showSortMenuDialog(it)
                    viewModel.onSortOptionsMenuShown()
                }

                uiState.showSourcesListMenu?.let {
                    showSourcesListDialog(it)
                    viewModel.onSourcesListMenuShown()
                }

                uiState.showDatePicker?.let {
                    showStartDatePicker(it)
                    viewModel.onDatePickerShown()
                }

                uiState.errorMsg?.let {
                    showErrorMsg(it)
                    viewModel.onErrorMsgShown()
                }
            }
        }
    }

    private fun reloadData() {
        mBinding?.rvList?.scrollToPosition(0)
        Handler(Looper.getMainLooper()).postDelayed({
            pagingAdapter?.refresh()
        }, 300)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_news, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort -> {
                viewModel.onSortOptionClicked()
            }
            R.id.action_filter_by_source -> {
                viewModel.onFilterBySourceOptionClicked()
            }

            R.id.action_filter_by_date -> {
                viewModel.onFilterByDateClicked()
            }
            R.id.action_favorites -> {
                // Todo: add implementation
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSortMenuDialog(items: List<SortByModel>) {
        context?.let { c ->
            val listItems = items.map { getString(it.text) }.toTypedArray()
            val selectedItem = items.indexOfFirst { it.isSelected }
            val mBuilder = AlertDialog.Builder(c)
            mBuilder.setTitle(getString(R.string.action_sort_by))
            mBuilder.setSingleChoiceItems(listItems, selectedItem) { dialogInterface, i ->
                viewModel.selectSortMode(items[i].mode)
                dialogInterface.dismiss()
            }
            mBuilder.setNegativeButton(R.string.action_cancel) { dialog, _ ->
                dialog.cancel()
            }

            val mDialog = mBuilder.create()
            mDialog.show()
        }

    }

    private fun showSourcesListDialog(items: List<SourceModel>) {
        context?.let { c ->
            val listItems = items.map { it.name }.toTypedArray()
            val selectedItem = items.indexOfFirst { it.isSelected }
            val mBuilder = AlertDialog.Builder(c)
            mBuilder.setTitle(getString(R.string.action_filter_by_source))
            mBuilder.setSingleChoiceItems(listItems, selectedItem) { dialogInterface, i ->
                viewModel.selectSource(items[i])
                dialogInterface.dismiss()
            }
            mBuilder.setNegativeButton(R.string.action_cancel) { dialog, _ ->
                dialog.cancel()
            }

            val mDialog = mBuilder.create()
            mDialog.show()
        }

    }

    private fun showStartDatePicker(defPeriod: Pair<Calendar, Calendar>) {
        context?.let { c ->
            val dialog = DatePickerDialog(
                c,
                { _, year, month, day -> run {
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, month)
                    selectedDate.set(Calendar.DAY_OF_MONTH, day)
                    showEndDatePicker(defPeriod, selectedDate)
                } },
                defPeriod.first.get(Calendar.YEAR),
                defPeriod.first.get(Calendar.MONTH),
                defPeriod.first.get(Calendar.DAY_OF_MONTH)
            )
            dialog.setTitle(R.string.select_start_date)
            dialog.show()
        }
    }

    private fun showEndDatePicker(defPeriod: Pair<Calendar, Calendar>, startDate: Calendar) {
        context?.let { c ->
            val dialog = DatePickerDialog(
                c,
                { _, year, month, day -> run {
                    val endDate = Calendar.getInstance()
                    endDate.set(Calendar.YEAR, year)
                    endDate.set(Calendar.MONTH, month)
                    endDate.set(Calendar.DAY_OF_MONTH, day)
                    viewModel.selectPeriod(startDate, endDate)
                } },
                defPeriod.second.get(Calendar.YEAR),
                defPeriod.second.get(Calendar.MONTH),
                defPeriod.second.get(Calendar.DAY_OF_MONTH)
            )
            dialog.setTitle(R.string.select_end_date)
            dialog.datePicker.minDate = startDate.timeInMillis
            dialog.show()
        }
    }

    private fun showErrorMsg(msg: String) {
        view?.let { v ->
            val snackbar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.action_cancel)) {
                snackbar.dismiss()
            }.show()
        }

    }

}
