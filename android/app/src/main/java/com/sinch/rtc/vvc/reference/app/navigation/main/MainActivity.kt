package com.sinch.rtc.vvc.reference.app.navigation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.tabs.TabLayout
import com.sinch.rtc.vvc.reference.app.MainNavGraphDirections
import com.sinch.rtc.vvc.reference.app.R
import com.sinch.rtc.vvc.reference.app.application.NoArgsRTCVoiceVideoRefAppAndroidViewModelFactory
import com.sinch.rtc.vvc.reference.app.databinding.ActivityMainBinding
import com.sinch.rtc.vvc.reference.app.utils.base.activity.ViewBindingActivity

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels {
        NoArgsRTCVoiceVideoRefAppAndroidViewModelFactory(application)
    }

    private val topDestinations = listOf(
        R.id.newCallFragment,
        R.id.callHistoryFragment
    )

    private val tabPositionToDestinationMap = mapOf(
        0 to R.id.newCallFragment,
        1 to R.id.callHistoryFragment
    )

    //Workaround for IllegalStateException thrown when trying to access navigation host usual way
    //https://stackoverflow.com/questions/50502269/illegalstateexception-link-does-not-have-a-navcontroller-set
    private val navHostFragment
        get() =
            supportFragmentManager.findFragmentById(R.id.mainNavigationHostFragment) as NavHostFragment

    override fun setupBinding(inflater: LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeNavigationEvents()
        viewModel.onViewCreated()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.settingsMenuItem -> {
                navHostFragment.navController.navigate(R.id.settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.settingsMenuItem)?.isVisible =
            topDestinations.contains(navHostFragment.navController.currentDestination?.id)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupNavigation() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                navigateToTab(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                navigateToTab(tab)
            }
        })

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.tabLayout.isVisible = topDestinations.contains(destination.id)
            if (topDestinations.indexOf(destination.id) >= 0) {
                binding.tabLayout.setScrollPosition(
                    topDestinations.indexOf(destination.id),
                    0f,
                    true
                )
            }
            invalidateOptionsMenu()
        }
    }

    private fun observeNavigationEvents() {
        viewModel.navigationEvents.observe(this) {
            when (it) {
                Login -> {
                    finish()
                    navHostFragment.navController.navigate(R.id.to_logged_out_flow)
                }
                Dashboard -> {
                    setupNavigation()
                }
                is IncomingCall -> {
                    navHostFragment.navController.navigate(MainNavGraphDirections.toIncomingCall(it.callId))
                }
            }
        }
    }

    private fun navigateToTab(tab: TabLayout.Tab?) {
        tabPositionToDestinationMap[tab?.position]?.let {
            navHostFragment.navController.apply {
                navigate(it, null, NavOptions.Builder().setPopUpTo(it, true).build())
            }
        }
    }

}