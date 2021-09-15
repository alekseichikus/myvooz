package ru.createtogether.myVooz.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myvoozkotlin.aboutNew.presentations.viewModel.OnBoardingViewModel
import com.example.myvoozkotlin.auth.viewModels.AuthViewModel
import com.example.myvoozkotlin.data.db.realmModels.AuthUserModel
import com.example.myvoozkotlin.helpers.Status
import ru.createtogether.myVooz.user.presentation.viewModel.UserViewModel
import com.google.firebase.iid.FirebaseInstanceId
import dagger.hilt.android.AndroidEntryPoint
import ru.createtogether.myVooz.R
import ru.createtogether.myVooz.databinding.FragmentSplashBinding


@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val onBoardingViewModel: OnBoardingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        if (onBoardingViewModel.isFirstSeen()) {
            findNavController().navigate(R.id.action_splashFragment_to_aboutNewFragment2)
        } else {
            if (userViewModel.getIdGroup() == 0)
                findNavController().navigate(
                    SplashFragmentDirections.actionSplashFragmentToSelectGroupFragment(
                        true
                    )
                )
            else {
                if (getCurrentUser() == null) {
                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                } else {
                    getCurrentUser()?.let {
                        authViewModel.authVk(
                            it.accessToken,
                            it.id,
                            it.idUniversity,
                            it.idGroup,
                            FirebaseInstanceId.getInstance().token!!
                        )
                    }
                }
            }
        }
    }

    private fun getCurrentUser(): AuthUserModel? {
        return userViewModel.getCurrentAuthUser()
    }

    private fun initObservers() {
        observeOnAuthResponse()
    }

    private fun observeOnAuthResponse() {
        authViewModel.authVkResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    if (it.data == null) {
                        userViewModel.removeCurrentUser()
                        findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                    }
                }
                Status.ERROR -> {
                    userViewModel.removeCurrentUser()
                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}