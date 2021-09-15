package ru.createtogether.myVooz

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.example.myvoozkotlin.fcm.FCMInstance
import com.example.myvoozkotlin.helpers.forView.NewDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import ru.createtogether.myVooz.databinding.ActivityMainBinding
import ru.createtogether.myVooz.fcm.FCMService


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var dialog: NewDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MyVoozKotlin)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureViews()
        initFCM()
    }

    private fun configureViews() {
        hideSystemUI()
    }

    private fun initFCM() {
        startService(Intent(this, FCMInstance::class.java))
        startService(Intent(this, FCMService::class.java))
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.root.setOnApplyWindowInsetsListener { v, insets ->
            v.updatePadding(bottom = insets.systemWindowInsetBottom)
            return@setOnApplyWindowInsetsListener insets
        }
    }

    fun showWait(isShow: Boolean) {
        if (dialog == null) {
            dialog = NewDialog(R.layout.dialog_fragment_loading)
        }
        dialog?.let {
            it.isCancelable = false
        }
        if (isShow) {
            if (!dialog!!.isAdded) {
                dialog!!.show(supportFragmentManager, null)
            }
        } else {
            if (dialog!!.isAdded) {
                dialog!!.dismiss()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }
}