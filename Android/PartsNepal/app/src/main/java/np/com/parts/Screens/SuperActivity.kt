package np.com.parts.Screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.ibrahimsn.lib.BuildConfig
import np.com.parts.R
import np.com.parts.databinding.ActivitySuperBinding
import np.com.parts.ViewModels.CartViewModel
import timber.log.Timber
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.activity.viewModels
import androidx.navigation.ui.setupWithNavController
import androidx.core.content.ContextCompat

@SuppressLint("StaticFieldLeak")
private lateinit var navController: NavController
private lateinit var binding: ActivitySuperBinding
private lateinit var cartViewModel: CartViewModel

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

         val cartViewModel by lazy {
            ViewModelProvider(this)[CartViewModel::class.java]
        }


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
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_of_super) as NavHostFragment
        navController = navHostFragment.navController

// Set up SmoothBottomBar

        setupBottomNavigation()
        observeCartBadge()








    }

    private fun setupBottomNavigation() {
        // Setup bottom navigation with NavController
        binding.bottomBar.setupWithNavController(navController)
        
        // Optional: Add animation for badge
        binding.bottomBar.setOnNavigationItemReselectedListener { /* Prevent reselection */ }
    }

    private fun observeCartBadge() {
        lifecycleScope.launch {
            cartViewModel.cartItemCount.collectLatest { count ->
                if (count > 0) {
                    binding.bottomBar.getOrCreateBadge(R.id.cartFragment).apply {
                        number = count
                        backgroundColor = ContextCompat.getColor(
                            this@SuperActivity, 
                            R.color.status_sending
                        )
                        isVisible = true
                    }
                } else {
                    binding.bottomBar.removeBadge(R.id.cartFragment)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }




}