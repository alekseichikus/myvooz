package ru.createtogether.myVooz.selectGroup.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.example.myvoozkotlin.groupOfUser.GroupOfUserFragmentDirections
import ru.createtogether.myVooz.BaseApp
import com.example.myvoozkotlin.helpers.Constants
import com.example.myvoozkotlin.helpers.Status
import com.example.myvoozkotlin.helpers.UtilsUI
import com.example.myvoozkotlin.helpers.show
import com.example.myvoozkotlin.search.SearchFragment
import com.example.myvoozkotlin.search.helpers.SearchEnum
import ru.createtogether.myVooz.selectGroup.presentation.viewModel.SelectGroupViewModel
import ru.createtogether.myVooz.user.presentation.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.createtogether.myVooz.MainActivity
import ru.createtogether.myVooz.R
import ru.createtogether.myVooz.databinding.FragmentSelectGroupBinding

@AndroidEntryPoint
class SelectGroupFragment : Fragment() {

    val args: SelectGroupFragmentArgs by navArgs()
    private var _binding: FragmentSelectGroupBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()
    private val selectGroupViewModel: SelectGroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureViews()
        setListeners()
        setData()
        initObservers()
        setPaddingTopMenu()
    }

    private fun setPaddingTopMenu() {
        binding.toolbar.setPadding(0, UtilsUI.getStatusBarHeight(resources), 0, 0)
    }

    private fun setData() {
        setNameUniversity()
        setNameGroup()
    }

    private fun setNameUniversity(){
        binding.tvUniversityName.text = selectGroupViewModel.nameUniversity ?: "Не выбрано"
    }

    private fun setNameGroup(){
        binding.tvGroupName.text = selectGroupViewModel.nameGroup ?: "Не выбрано"
    }

    private fun configureViews() {
        if (args.isFirst) {
            binding.ivPreview.show()
            binding.tvTitle.show()
        } else
            initToolbar()
    }

    private fun setListeners() {
        setUniversityClickListener()
        setGroupClickListener()
        setSaveClickListener()
        setUniversityResultListener()
        setGroupResultListener()
    }

    private fun setDefaultGroupValue(){
        selectGroupViewModel.idGroup = null
        selectGroupViewModel.nameGroup = null
        binding.tvGroupName.text = getString(R.string.not_selected)
    }

    private fun setUniversityResultListener(){
        parentFragmentManager.setFragmentResultListener(SearchFragment.REQUEST_UNIVERSITY, this) { key, bundle ->
            val universityName = bundle.getString(SearchFragment.KEY_FULL_NAME, "null")
            binding.tvUniversityName.text = universityName
            selectGroupViewModel.idUniversity = bundle.getInt(SearchFragment.KEY_ID)
            selectGroupViewModel.nameUniversity = universityName
            setDefaultGroupValue()
            setData()
        }
    }

    private fun setGroupResultListener(){
        parentFragmentManager.setFragmentResultListener(SearchFragment.REQUEST_GROUP, this) { key, bundle ->
            val groupName = bundle.getString(SearchFragment.KEY_FULL_NAME, "null")
            binding.tvGroupName.text = groupName
            selectGroupViewModel.idGroup = bundle.getInt(SearchFragment.KEY_ID)
            selectGroupViewModel.nameGroup = groupName
            setData()
        }
    }

    private fun setGroupClickListener() {
        binding.clGroupButton.setOnClickListener {
            if (selectGroupViewModel.idUniversity == null)
                UtilsUI.makeToast(getString(R.string.select_university))
            else
                loadSearchFragment(SearchEnum.GROUP.ordinal, selectGroupViewModel.idUniversity!!)
        }
    }

    private fun setUniversityClickListener() {
        binding.clUniversityButton.setOnClickListener {
            loadSearchFragment(SearchEnum.UNIVERSITY.ordinal, 0)
        }
    }

    private fun setSaveClickListener() {
        binding.cvSaveButton.setOnClickListener {
            when {
                selectGroupViewModel.idUniversity == null ->
                    UtilsUI.makeToast(getString(R.string.toast_select_university))
                selectGroupViewModel.idGroup == null ->
                    UtilsUI.makeToast(getString(R.string.toast_select_group))
                else -> {
                    saveSelectValue()
                    if (args.isFirst) {
                        findNavController().navigate(SelectGroupFragmentDirections.actionSelectGroupFragmentToMainFragment())
                    } else {
                        if (userViewModel.getCurrentAuthUser() == null) {
                            findNavController().popBackStack()
                        } else {
                            userViewModel.getCurrentAuthUser()?.let {
                                userViewModel.changeIdGroupUser(
                                    it.accessToken,
                                    it.id,
                                    selectGroupViewModel.nameGroup!!,
                                    selectGroupViewModel.idGroup!!
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initObservers() {
        observeOnChangeIdGroupResponse()
    }

    private fun saveSelectValue() {
        BaseApp.getSharedPref().edit()
            .putString(Constants.APP_PREFERENCES_USER_GROUP_NAME, selectGroupViewModel.nameGroup)
            .apply()
        BaseApp.getSharedPref().edit()
            .putInt(Constants.APP_PREFERENCES_USER_GROUP_ID, selectGroupViewModel.idGroup!!).apply()

        BaseApp.getSharedPref().edit().putString(
            Constants.APP_PREFERENCES_USER_UNIVERSITY_NAME,
            selectGroupViewModel.nameUniversity
        ).apply()
        BaseApp.getSharedPref().edit().putInt(
            Constants.APP_PREFERENCES_USER_UNIVERSITY_ID,
            selectGroupViewModel.idUniversity!!
        ).apply()
    }

    private fun loadSearchFragment(typeSearch: Int, addParam: Int) {
        findNavController().navigate(SelectGroupFragmentDirections.actionSelectGroupFragmentToSearchFragment(typeSearch, addParam))
    }

    private fun initToolbar() {
        binding.toolbar.title = getString(R.string.title_select_group)
        if (!args.isFirst) {
            addBackButton()
        }
    }

    private fun observeOnChangeIdGroupResponse() {
        userViewModel.changeIdGroupUserResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    (requireActivity() as MainActivity).showWait(true)
                }
                Status.SUCCESS -> {
                    (requireActivity() as MainActivity).showWait(false)
                    findNavController().popBackStack()
                }
                Status.ERROR -> {
                    (requireActivity() as MainActivity).showWait(false)
                }
            }
        })
    }

    private fun addBackButton() {
        binding.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}