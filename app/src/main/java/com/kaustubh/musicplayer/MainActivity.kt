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
import com.kaustubh.musicplayer.service.MusicService

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var miniPlayer: View
    private lateinit var fullPlayer: View
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        initViews()
        setupWindowInsets()
        requestPermissions()
        setupBottomNavigation()
        
        // Start with home fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }
    
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        miniPlayer = findViewById(R.id.mini_player)
        fullPlayer = findViewById(R.id.full_player)
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
                R.id.nav_equalizer -> {
                    loadFragment(EqualizerFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    fun showMiniPlayer() {
        miniPlayer.visibility = View.VISIBLE
    }
    
    fun hideMiniPlayer() {
        miniPlayer.visibility = View.GONE
    }
    
    fun showFullPlayer() {
        fullPlayer.visibility = View.VISIBLE
    }
    
    fun hideFullPlayer() {
        fullPlayer.visibility = View.GONE
    }
}