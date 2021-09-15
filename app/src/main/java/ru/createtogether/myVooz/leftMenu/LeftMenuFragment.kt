package ru.createtogether.myVooz.leftMenu

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.myvoozkotlin.data.db.realmModels.AuthUserModel
import com.example.myvoozkotlin.helpers.*
import com.example.myvoozkotlin.home.HomeFragment
import com.example.myvoozkotlin.leftMenu.presentation.ConfirmLogoutDialogFragment
import com.example.myvoozkotlin.main.presentation.MainFragment
import com.example.myvoozkotlin.main.presentation.MainFragmentDirections
import ru.createtogether.myVooz.user.presentation.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.createtogether.myVooz.BaseApp
import ru.createtogether.myVooz.R
import ru.createtogether.myVooz.auth.AuthActivity
import ru.createtogether.myVooz.databinding.FragmentLeftMenuBinding


@AndroidEntryPoint
class LeftMenuFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance(): LeftMenuFragment {
            return LeftMenuFragment()
        }
    }

    private var _binding: FragmentLeftMenuBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeftMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureViews()
        setListeners()
        initObservers()
        initBlocks(userViewModel.getCurrentAuthUser())
    }

    private fun configureViews() {
        setPaddingTopMenu()
    }

    private fun setListeners() {
        setVkClickListener()
        setAboutClickListener()
        setLeftMenuClickListener()
        setNotificationClickListener()
        setSearchEmptyAuditoryClickListener()
        BaseApp.getSharedPref().registerOnSharedPreferenceChangeListener(this)
    }

    private fun setPaddingTopMenu() {
        binding.navigationViewContainer.setPadding(0, UtilsUI.getStatusBarHeight(resources), 0, 0)
    }

    private fun setLeftMenuClickListener() {
        binding.cvCloseButton.setOnClickListener {
            (parentFragment as? MainFragment)?.openHomeList()
        }
    }

    private fun setVkClickListener() {
        binding.llVkSocialButton.setOnClickListener {
            openLink(Constants.APP_PREFERENCES_VK_SOCIAL_LINK)
        }
    }

    private fun setSearchEmptyAuditoryClickListener() {
        binding.llSearchEmptyAuditoryButton.setOnClickListener {
            openSearchEmptyAuditoryFragment()
        }
    }

    private fun setNotificationClickListener() {
        binding.cvNotificationButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_notificationFragment)
        }
    }

    private fun initObservers() {
        observeOnAuthUserChangeResponse()
    }

    private fun observeOnAuthUserChangeResponse() {
        userViewModel.authUserChangeResponse.observe(viewLifecycleOwner, {
            initBlocks(userViewModel.getCurrentAuthUser())
        })
    }

    private fun setAboutClickListener() {
        binding.llAboutButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_aboutFragment2)
        }
    }

    private fun openAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intent)
    }

    private fun openSelectGroupFragment() {
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToSelectGroupFragment(false))
    }

    private fun openCreateGroupOfUserFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_createGroupOfUserFragment)
    }

    private fun openUserFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_userFragment)
    }

    private fun openSearchEmptyAuditoryFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_searchEmptyAuditoryFragment)
    }

    private fun openGroupOfUserFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_groupOfUserFragment)
    }

    private fun openInviteGroupOfUserFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_inviteGroupOfUserFragment)
    }

    private fun initGroupOfUserBlock(authUserModel: AuthUserModel?) {
        binding.apply {
            when (Utils.getAuthorisationState(authUserModel)) {
                AuthorizationState.UNAUTORIZATE -> {
                    showCreateGOUButton(false)
                    showInviteGOUButton(false)
                    showGOUButton(false)
                }
                AuthorizationState.AUTORIZATE -> {
                    showCreateGOUButton(true)
                    showInviteGOUButton(true)
                    showGOUButton(false)
                }
                AuthorizationState.GROUP_AUTORIZATE -> {
                    showCreateGOUButton(false)
                    showInviteGOUButton(false)
                    showGOUButton(true)

                    cvGOUButton.tvGOUName.text = authUserModel!!.groupOfUser!!.name
                    cvGOUButton.tvGOUGroup.text = authUserModel.groupOfUser!!.nameGroup

                    showPhotoGOUImage(true, authUserModel.groupOfUser!!.image)
                }
            }
        }
    }

    private fun initUserBlock(authUserModel: AuthUserModel?) {
        when (Utils.getAuthorisationState(authUserModel)) {
            AuthorizationState.UNAUTORIZATE -> {
                showPhotoUserImage(false, "")

                showUserButton(false)
                showLogoutButton(false)
                showLoginButton(true)
                showSelectGroupButton(true)

                setNameUser(requireContext().getString(R.string.default_user_name))
                setNameGroup(userViewModel.getNameGroup())
            }
            AuthorizationState.AUTORIZATE -> {
                showPhotoUserImage(true, authUserModel!!.photo)

                showUserButton(true)
                showLogoutButton(true)
                showLoginButton(false)
                showSelectGroupButton(true)

                setNameUser(authUserModel.lastName + " " + authUserModel.firstName[0] + ".")
                setNameGroup(authUserModel.nameGroup)
            }
            AuthorizationState.GROUP_AUTORIZATE -> {
                showPhotoUserImage(true, authUserModel!!.photo)

                showUserButton(true)
                showLogoutButton(true)
                showLoginButton(false)
                showSelectGroupButton(false)

                setNameUser("${authUserModel.lastName} ${authUserModel.firstName[0]}.")
                setNameGroup(authUserModel.groupOfUser!!.nameGroup)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals(Constants.APP_PREFERENCES_USER_GROUP_ID)){
            initUserBlock(userViewModel.getCurrentAuthUser())
        }
    }

    private fun showUserButton(state: Boolean) =
        showButton(binding.llProfileSettingButton.id, state) { openUserFragment() }

    private fun showLogoutButton(state: Boolean) =
        showButton(binding.llLogoutButton.id, state) {
            val fragment = ConfirmLogoutDialogFragment()
            fragment.show(parentFragmentManager,
                ConfirmLogoutDialogFragment::javaClass.javaClass.simpleName) }

    private fun showLoginButton(state: Boolean) =
        showButton(binding.llAutorizationButton.id, state) { openAuthActivity() }

    private fun showCreateGOUButton(state: Boolean) =
        showButton(binding.cvCreateGOUButton.id, state) { openCreateGroupOfUserFragment() }

    private fun showInviteGOUButton(state: Boolean) =
        showButton(binding.cvInviteGOUButton.id, state) { openInviteGroupOfUserFragment() }

    private fun showGOUButton(state: Boolean) =
        showButton(binding.cvGOUButton.root.id, state) { openGroupOfUserFragment() }

    private fun showSelectGroupButton(state: Boolean) =
        showButton(binding.llSelectGroup.id, state) { openSelectGroupFragment() }

    private fun showPhotoUserImage(state: Boolean, url: String) =
        showImage(binding.ivPhotoUser.id, state, url)

    private fun showPhotoGOUImage(state: Boolean, url: String) =
        showImage(binding.cvGOUButton.ivGOUPreview.id, state, url)

    private fun setNameGroup(nameGroup: String) {
        binding.tvGroupName.text = nameGroup
    }

    private fun setNameUser(nameUser: String) {
        binding.tvUserName.text = nameUser
    }

    private fun showImage(
        @IdRes idRes: Int,
        state: Boolean,
        url: String?
    ) {
        binding.root.findViewById<View>(idRes).apply {
            if (state) {
                show()
                Glide.with(requireContext())
                    .load(url)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(HomeFragment.ANIMATE_TRANSITION_DURATION))
                    .into(this as ImageView)
            } else
                hide()
        }
    }

    private fun showButton(
        @IdRes idRes: Int,
        state: Boolean,
        itemClickListener: () -> Unit
    ) {
        binding.root.findViewById<View>(idRes).apply {
            if (state) {
                show()
                setOnClickListener {
                    itemClickListener.invoke()
                }
            } else
                hide()
        }
    }

    private fun initBlocks(authUserModel: AuthUserModel?) {
        initGroupOfUserBlock(authUserModel)
        initUserBlock(authUserModel)
    }

    private fun openLink(link: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}