package com.kaustubh.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kaustubh.musicplayer.fragments.EqualizerFragment
import com.kaustubh.musicplayer.fragments.HomeFragment
import com.kaustubh.musicplayer.fragments.SearchFragment
import com.kaustubh.musicplayer.fragments.LibraryFragment
import com.kaustubh.musicplayer.service.MusicService
import com.kaustubh.musicplayer.ui.MiniPlayerController

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var miniPlayer: View
    private lateinit var miniPlayerController: MiniPlayerController
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupStatusBar()
        initViews()
        setupWindowInsets()
        requestPermissions()
        setupBottomNavigation()
        
        // Start with home fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }
      private fun setupStatusBar() {
        // Configure status bar for Apple Music style
        window.statusBarColor = ContextCompat.getColor(this, R.color.apple_status_bar)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.apple_surface_primary)
        
        // Make status bar content light (white icons/text) for dark background
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Clear light status bar flag for dark background
            var flags = window.decorView.systemUiVisibility
            flags = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window.decorView.systemUiVisibility = flags
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Clear light navigation bar flag for dark background
            var flags = window.decorView.systemUiVisibility
            flags = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            window.decorView.systemUiVisibility = flags
        }
    }
    
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        miniPlayer = findViewById(R.id.mini_player)
        miniPlayerController = MiniPlayerController(miniPlayer, this, this)
    }
      private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            
            // Apply proper padding to ensure status bar is visible
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            
            // Ensure bottom navigation respects navigation bar
            bottomNavigation.setPadding(
                bottomNavigation.paddingLeft,
                bottomNavigation.paddingTop,
                bottomNavigation.paddingRight,
                bottomNavigation.paddingBottom + systemBars.bottom
            )
            
            insets
        }
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.nav_library -> {
                    loadFragment(LibraryFragment())
                    true
                }
                R.id.nav_equalizer -> {
                    loadFragment(EqualizerFragment())
                    true
                }
                else -> false
            }
        }
        
        // Set default selection
        bottomNavigation.selectedItemId = R.id.nav_home
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    fun switchToSearchTab() {
        bottomNavigation.selectedItemId = R.id.nav_search
        loadFragment(SearchFragment())
    }
    
    fun showMiniPlayer() {
        miniPlayerController.show()
    }
    
    fun hideMiniPlayer() {
        miniPlayerController.hide()
    }
}