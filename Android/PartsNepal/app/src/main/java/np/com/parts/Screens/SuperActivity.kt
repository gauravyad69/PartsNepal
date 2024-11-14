package np.com.parts.Screens

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import np.com.parts.R
import np.com.parts.databinding.ActivitySuperBinding

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }


        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isDarkMode) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.mainBackground)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.mainBackground)
            } else {
                window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.mainColor)
            }
        }

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





}