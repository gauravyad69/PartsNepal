package np.com.parts.Screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import me.ibrahimsn.lib.BuildConfig
import np.com.parts.API.TokenManager
import np.com.parts.R
import np.com.parts.Screens.StartingScreens.LoginFragment
import np.com.parts.databinding.ActivitySuperBinding
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
private lateinit var navController: NavController
private lateinit var binding: ActivitySuperBinding


class SuperActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets }


        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.setDecorFitsSystemWindows(false)
//            window.insetsController?.let { controller ->
//                controller.hide(WindowInsets.Type.navigationBars())
//                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            }
//        } else {
//            @Suppress("DEPRECATION")
//            window.decorView.systemUiVisibility = (
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                            or View.SYSTEM_UI_FLAG_FULLSCREEN
//                    )
//        }


//        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
//        val window = window
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (isDarkMode) {
//                window.statusBarColor = ContextCompat.getColor(this, R.color.background_main)
//                window.navigationBarColor = ContextCompat.getColor(this, R.color.background_main)
//            } else {
//                window.statusBarColor = ContextCompat.getColor(this, R.color.color_main)
//                window.navigationBarColor = ContextCompat.getColor(this, R.color.color_main)
//            }
//        }

        // Initialize NavController using the correct ID
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_of_super) as NavHostFragment
        navController = navHostFragment?.navController!!

// Set up SmoothBottomBar

        setupSmoothBottomMenu()








    }

    private fun setupSmoothBottomMenu() {
        // Set up the menu for SmoothBottomBar
        val popupMenu = PopupMenu(this, binding.bottomBar)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)

        // Attach the SmoothBottomBar to the NavController
        binding.bottomBar.setupWithNavController(popupMenu.menu, navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun startFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            // Replace the container with the new fragment
            replace(R.id.container, fragment)

            // Optional: Add to back stack if you want back navigation
             addToBackStack(null)
        }}



}