package com.example.myvoozkotlin.home

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.myvoozkotlin.data.db.realmModels.AuthUserModel
import ru.createtogether.myVooz.home.model.Lesson
import ru.createtogether.myVooz.BaseApp
import com.example.myvoozkotlin.helpers.*
import com.example.myvoozkotlin.home.adapters.NewsAdapter
import com.example.myvoozkotlin.home.adapters.ScheduleDayAdapter
import com.example.myvoozkotlin.home.adapters.WeekAdapter
import com.example.myvoozkotlin.home.helpers.OnDatePicked
import com.example.myvoozkotlin.home.helpers.OnDayPicked
import com.example.myvoozkotlin.home.helpers.OnStoryClick
import com.example.myvoozkotlin.home.helpers.ScheduleState
import com.example.myvoozkotlin.home.viewModels.NewsViewModel
import com.example.myvoozkotlin.home.viewModels.ScheduleViewModel
import com.example.myvoozkotlin.main.presentation.MainFragment
import ru.createtogether.myVooz.user.presentation.viewModel.UserViewModel
import com.example.myvoozkotlin.models.news.News
import dagger.hilt.android.AndroidEntryPoint
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import ru.createtogether.myVooz.R
import ru.createtogether.myVooz.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class HomeFragment : Fragment(), OnDayPicked, OnDatePicked,
    OnSharedPreferenceChangeListener, OnStoryClick {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val newsViewModel: NewsViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var scheduleState = ScheduleState.SHOW

    private lateinit var calendar: Calendar
    private lateinit var currentCalendar: Calendar
    private val END_SCALE = 0.8f

    companion object {
        const val WEEK_RV_ANIMATE_DURATION: Long = 250
        const val ANIMATE_TRANSITION_DURATION: Int = 300

        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        BaseApp.getSharedPref().registerOnSharedPreferenceChangeListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar = Calendar.getInstance()
        currentCalendar = calendar.clone() as Calendar

        configureViews()

        initObservers()

        initAuthUser(userViewModel.getCurrentAuthUser())

        setListeners()
    }

    private fun setPaddingTopMenu() {
        binding.appBarLayout.setPadding(0, UtilsUI.getStatusBarHeight(resources), 0, 0)
    }

    private fun configureViews() {
        setHasOptionsMenu(true)
        initWeekAdapter(calendar)
        configureToolbar()
        checkDateAnother()
        setPaddingTopMenu()
    }

    private fun initAuthUser(authUserModel: AuthUserModel?) {
        initSiteButton()

        when (Utils.getAuthorisationState(authUserModel)) {
            AuthorizationState.UNAUTORIZATE -> {
                loadSchedule(
                    BaseApp.getSharedPref().getInt(Constants.APP_PREFERENCES_USER_GROUP_ID, 0)
                )
            }
            AuthorizationState.AUTORIZATE -> {
                loadSchedule(authUserModel!!.idGroup)
            }
            AuthorizationState.GROUP_AUTORIZATE -> {
                loadSchedule(authUserModel!!.groupOfUser!!.idGroup)
            }
        }
    }

    private fun setListeners() {

        binding.ibDateButton.setOnClickListener {
            if (binding.rvWeek.isVisible) {
                binding.apply {
                    rvWeek.clearAnimation()
                    rvWeek.hide()
                }
            } else {
                binding.rvWeek.show()
                startWeekRVAnimate()
            }
        }

        setSiteClickListener()
    }

    private fun initSiteButton() {
        Glide.with(requireContext())
            .load("https://myvooz.ru/public/images/university/icon-ugatu-100.png")
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade(ANIMATE_TRANSITION_DURATION))
            .into(binding.ivSiteButton)
        setSiteClickListener()
    }

    private fun setSiteClickListener() {
        binding.cvSiteButton.setOnClickListener {
            openLink("https://www.ugatu.su/")
        }
    }

    private fun openLink(link: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
    }

    private fun loadSchedule(idGroup: Int) {
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        dayOfWeek = if (dayOfWeek == 1) 6 else dayOfWeek - 2
        scheduleViewModel.loadScheduleDay(idGroup, weekOfYear, dayOfWeek)
    }


    private fun initScheduleDayAdapter(lessons: List<List<Lesson>>) {
        if (binding.rvLesson.adapter == null) {
            binding.rvLesson.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvLesson.adapter = ScheduleDayAdapter(requireContext(), lessons)
        } else {
            (binding.rvLesson.adapter as? ScheduleDayAdapter)?.update(lessons)
        }
    }

    private fun initWeekAdapter(calendar: Calendar) {
        if (binding.rvWeek.adapter == null) {
            binding.rvWeek.layoutManager = GridLayoutManager(requireContext(), 7)
            binding.rvWeek.adapter = WeekAdapter(calendar, this)
        } else {
            (binding.rvWeek.adapter as? WeekAdapter)?.update(calendar)
        }
    }

    private fun initObservers() {
        observeOnScheduleDayResponse()
    }

    private fun observeOnScheduleDayResponse() {
        observeOnAuthUserChangeResponse()
        scheduleViewModel.scheduleDayResponse.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.show()
                    binding.rvLesson.hide()
                    (binding.root.findViewById(R.id.ll_empty) as View).hide()
                }
                Status.SUCCESS -> {
                    binding.progressBar.hide()

                    if (it.data == null) {

                    } else {
                        initScheduleDayAdapter(it.data)
                        if (it.data.isEmpty()) {
                            binding.rvLesson.hide()
                            (binding.root.findViewById(R.id.ll_empty) as View).show()
                        } else {
                            binding.rvLesson.show()
                            (binding.root.findViewById(R.id.ll_empty) as View).hide()
                        }
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.hide()
                }
            }
        })
    }

    private fun checkDateAnother() {
        binding.tvDateTitle.text =
            calendar.get(Calendar.DAY_OF_MONTH).toString() + " " + getNameWithPattern(
                calendar,
                "MMM"
            )
        val dayName = getNameWithPattern(calendar, "EEEE")
        binding.tvDayTitle.text = dayName.substring(0, 1).toUpperCase() + dayName.substring(1)
    }

    private fun getNameWithPattern(calendar: Calendar, pattern: String): String {
        val date = SimpleDateFormat(pattern)
        return date.format(calendar.time)
    }

    private fun startWeekRVAnimate() {
        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = WEEK_RV_ANIMATE_DURATION
        animation.fillAfter = true
        binding.rvWeek.startAnimation(animation)
    }

    private fun endWeekRVAnimate() {
        binding.rvWeek.clearAnimation()
        binding.rvWeek.hide()
    }

    override fun onDayClick(position: Int) {
        if (position == 6) {
            fragmentManager?.let {
                DatePickerDialogFragment.newInstance(calendar, this)
                    .show(it, DatePickerDialogFragment.javaClass.simpleName)
            }
        } else {
            calendar = Utils.getCalendarDayOfWeek(calendar, position)
            if (binding.rvWeek.adapter != null) {
                (binding.rvWeek.adapter as? WeekAdapter)!!.update(calendar)
                endWeekRVAnimate()

                checkDateAnother()
                loadSchedule(
                    BaseApp.getSharedPref().getInt(Constants.APP_PREFERENCES_USER_GROUP_ID, 0)
                )
            }
        }
    }

    override fun onDateCalendarClick(calendar: Calendar) {
        this.calendar = calendar
        if (binding.rvWeek.adapter != null) {
            (binding.rvWeek.adapter as WeekAdapter).update(calendar)
            endWeekRVAnimate()

            checkDateAnother()
            Utils.getAuthorisationState(userViewModel.getCurrentAuthUser()).let {
                when (Utils.getAuthorisationState(userViewModel.getCurrentAuthUser())) {
                    AuthorizationState.UNAUTORIZATE -> loadSchedule(
                        BaseApp.getSharedPref().getInt(Constants.APP_PREFERENCES_USER_GROUP_ID, 0)
                    )
                    AuthorizationState.GROUP_AUTORIZATE -> loadSchedule(userViewModel.getCurrentAuthUser()!!.groupOfUser!!.idGroup)
                    AuthorizationState.AUTORIZATE -> loadSchedule(
                        BaseApp.getSharedPref().getInt(Constants.APP_PREFERENCES_USER_GROUP_ID, 0)
                    )
                }

            }
        }
    }

    private fun observeOnAuthUserChangeResponse() {
        userViewModel.authUserChangeResponse.observe(viewLifecycleOwner, {
            initAuthUser(userViewModel.getCurrentAuthUser())
        })
    }

    private fun loadNoteFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_noteListFragment)
    }

    private fun configureToolbar() {
        setHasOptionsMenu(true)
        addBackButton()
        binding.toolbar.inflateMenu(R.menu.menu_home)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_note -> loadNoteFragment()
            }
            true
        }
    }

    private fun addBackButton() {
        binding.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_user_circle)
        binding.toolbar.setNavigationOnClickListener { (parentFragment as MainFragment).openLeftMenuList() }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(Constants.APP_PREFERENCES_USER_GROUP_ID)) {
            initAuthUser(null)
        }
    }

    override fun onStoryClick(stories: News) {
        val myStories = ArrayList<MyStory>()
        for (story in stories.stories) {
            myStories.add(
                MyStory(
                    story.image,
                    Calendar.getInstance().time
                )
            )
            println("----" + story.image)
        }
        StoryView.Builder(requireActivity().getSupportFragmentManager())
            .setStoriesList(myStories)
            .setStoryDuration(5000)
            .setTitleText(stories.name)
            .setTitleLogoUrl(stories.logoImage)
            .setSubtitleText("Медиацентр")
            .setStoryClickListeners(object : StoryClickListeners {
                override fun onDescriptionClickListener(position: Int) {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(stories.link))
                    startActivity(browserIntent)
                }

                override fun onTitleIconClickListener(position: Int) {}
            })
            .setOnStoryChangedCallback {
                //Toast.makeText(context, position + "", Toast.LENGTH_SHORT).show();
            }
            .setStartingIndex(0)
            .build()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}