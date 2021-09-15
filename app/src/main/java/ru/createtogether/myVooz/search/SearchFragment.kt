package com.example.myvoozkotlin.search

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.annotation.Nullable
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myvoozkotlin.helpers.Status
import com.example.myvoozkotlin.helpers.UtilsUI
import com.example.myvoozkotlin.helpers.hide
import com.example.myvoozkotlin.helpers.show
import com.example.myvoozkotlin.models.SearchItem
import com.example.myvoozkotlin.search.adapters.SearchAdapter
import com.example.myvoozkotlin.search.helpers.OnSearchItemPicked
import com.example.myvoozkotlin.search.helpers.SearchEnum
import com.example.myvoozkotlin.search.viewModels.SearchViewModel
import ru.createtogether.myVooz.selectGroup.presentation.viewModel.SelectGroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.createtogether.myVooz.R
import ru.createtogether.myVooz.databinding.FragmentSearchBinding
import ru.createtogether.myVooz.selectGroup.presentation.SelectGroupFragmentArgs


@AndroidEntryPoint
class SearchFragment : Fragment(), OnSearchItemPicked {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by viewModels()
    val args: SearchFragmentArgs by navArgs()

    lateinit var searchHandler: Handler
    lateinit var searchRunnable: Runnable
    lateinit var searchView: SearchView

    companion object {
        const val SEARCH_TIME_DELAY = 700L
        const val DEFAULT_MIN_LENGTH = 3
        const val CONSTANT_WITH_REQUEST = "withRequest"
        const val CONSTANT_MIN_LENGTH = "min_length"
        const val REQUEST_UNIVERSITY = "search_response_university"
        const val REQUEST_GROUP = "search_response_group"
        const val REQUEST_OBJECT = "search_response_object"
        const val REQUEST_CORPUS = "search_response_corpus"
        const val KEY_FULL_NAME = "fullName"
        const val KEY_ID = "id"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureViews()
        setListeners()
        initRunnable()
    }

    private fun configureViews() {
        initToolbar()
        initObservers()
        hideKeyBoard()
        setPaddingTopMenu()
    }

    private fun setPaddingTopMenu() {
        binding.root.setPadding(0, UtilsUI.getStatusBarHeight(resources), 0, 0)
    }

    private fun setListeners() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchHandler.removeCallbacks(searchRunnable)
                searchHandler.postDelayed(searchRunnable, SEARCH_TIME_DELAY)
                return true
            }

        })
    }

    private fun initRunnable() {
        searchHandler = Handler()
        searchRunnable = Runnable {
            if (searchView.query.toString().length < args.minLength) {
                binding.progressBar.hide()
                binding.root.findViewById<View>(R.id.ll_enter_value).show()
                binding.root.findViewById<View>(R.id.ll_empty).hide()
                binding.rvSearchList.hide()
            } else {
                when (args.type) {
                    SearchEnum.UNIVERSITY.ordinal -> {
                        searchViewModel.loadUniversityList(searchView.query.toString())
                    }
                    SearchEnum.GROUP.ordinal -> {
                        searchViewModel.loadGroupList(searchView.query.toString(), args.addValue)
                    }
                    SearchEnum.CORPUS.ordinal -> {
                        searchViewModel.loadCorpusList(searchView.query.toString(), args.addValue)
                    }
                    SearchEnum.OBJECT.ordinal -> {
                        searchViewModel.loadObjectList(searchView.query.toString(), args.addValue)
                    }
                }
            }
        }
    }

    private fun initObservers() {
        observeOnNewsResponse()
    }

    private fun observeOnNewsResponse() {
        searchViewModel.searchResponse.observe(viewLifecycleOwner, Observer {
            searchHandler.removeCallbacks(searchRunnable)
            when (it.status) {
                Status.LOADING -> {
                    binding.root.findViewById<View>(R.id.ll_enter_value).hide()
                    binding.root.findViewById<View>(R.id.ll_empty).hide()
                    binding.rvSearchList.hide()
                    binding.progressBar.show()
                }
                Status.SUCCESS -> {
                    binding.progressBar.hide()

                    if (it.data == null) {

                    } else {
                        initSearchAdapter(it.data)
                        if (it.data.isEmpty()) {
                            binding.root.findViewById<View>(R.id.ll_empty).show()
                            binding.rvSearchList.hide()
                        } else {
                            binding.root.findViewById<View>(R.id.ll_empty).hide()
                            binding.rvSearchList.show()
                        }
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.hide()
                }
            }
        })
    }

    private fun initSearchAdapter(searchItem: List<SearchItem>) {
        if (binding.rvSearchList.adapter == null) {
            binding.rvSearchList.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvSearchList.adapter = SearchAdapter(searchItem, this)
            (binding.rvSearchList.adapter as? SearchAdapter)?.update(searchItem)
        } else {
            (binding.rvSearchList.adapter as? SearchAdapter)?.update(searchItem)
        }
    }

    private fun initToolbar() {
        addBackButton()
        binding.toolbar.title = getString(R.string.title_search)
        binding.toolbar.inflateMenu(R.menu.menu_search)
        val menuItem = binding.toolbar.menu.findItem(R.id.item_search)
        searchView = MenuItemCompat.getActionView(menuItem) as SearchView
    }

    private fun addBackButton() {
        binding.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
            hideKeyBoard()
            requireActivity().onBackPressed()
        }
    }

    override fun onSearchItemClick(searchItem: SearchItem) {
        val bundle = Bundle()
        if (searchItem.fullName.isNullOrEmpty())
            bundle.putString(KEY_FULL_NAME, searchItem.name)
        else
            bundle.putString(KEY_FULL_NAME, searchItem.fullName)
        bundle.putInt(KEY_ID, searchItem.id)

        when (args.type) {
            SearchEnum.UNIVERSITY.ordinal -> {
                parentFragmentManager.setFragmentResult(REQUEST_UNIVERSITY, bundle)
            }
            SearchEnum.GROUP.ordinal -> {
                parentFragmentManager.setFragmentResult(REQUEST_GROUP, bundle)
            }
            SearchEnum.CORPUS.ordinal -> {
                parentFragmentManager.setFragmentResult(REQUEST_CORPUS, bundle)
            }
            SearchEnum.OBJECT.ordinal -> {
                parentFragmentManager.setFragmentResult(REQUEST_OBJECT, bundle)
            }
        }
        hideKeyBoard()
        requireActivity().onBackPressed()
    }

    private fun hideKeyBoard() {
        val inputMethodManager: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (requireActivity().currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(
                requireActivity().currentFocus!!.windowToken,
                0
            )
        }
    }
}