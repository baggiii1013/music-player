package com.kaustubh.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        
        // Enable edge-to-edge display first
        enableEdgeToEdge()
        setupStatusBar()
        
        setContentView(R.layout.activity_main)
        
        initViews()
        setupWindowInsets()
        requestPermissions()
        setupBottomNavigation()
        
        // Start with home fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }    private fun setupStatusBar() {
        // Make the app truly edge-to-edge with transparent status bar
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        // Configure status bar content color for better visibility
        val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
        windowInsetsController?.let { controller ->
            // Use light content (white icons) for dark backgrounds
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }
    
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        miniPlayer = findViewById(R.id.mini_player)
        miniPlayerController = MiniPlayerController(miniPlayer, this, this)
    }    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            // Apply status bar padding to the fragment container for proper content spacing
            val fragmentContainer = findViewById<View>(R.id.fragment_container)
            fragmentContainer.setPadding(
                0, // Remove horizontal padding to allow full-width gradients
                statusBars.top,
                0,
                0
            )
            
            // Ensure bottom navigation respects navigation bar and gets proper spacing
            bottomNavigation.setPadding(
                bottomNavigation.paddingLeft,
                bottomNavigation.paddingTop,
                bottomNavigation.paddingRight,
                bottomNavigation.paddingBottom + navigationBars.bottom
            )
            
            insets
        }
    }
      private fun requestPermissions() {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.READ_MEDIA_AUDIO)
            
            // Only request WRITE_EXTERNAL_STORAGE for Android 9 and below
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        
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