package np.com.parts.Screens

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.navigation.ui.setupWithNavController
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import np.com.parts.API.TokenManager
import np.com.parts.Screens.StartingScreens.VerifyEmailFragment
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
private lateinit var navController: NavController
private lateinit var binding: ActivitySuperBinding
@AndroidEntryPoint
class SuperActivity : AppCompatActivity() {

    private val cartViewModel: CartViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupStatusBar()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets }




        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }



        // Initialize NavController using the correct ID
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_of_super) as NavHostFragment
        navController = navHostFragment.navController

// Set up SmoothBottomBar

        setupBottomNavigation()
        observeCartBadge()



        if (tokenManager.hasToken()){
            val isVerified = Firebase.auth.currentUser?.isEmailVerified
            if (isVerified!=true){
                startFragment(VerifyEmailFragment())
            }
        }
    }



    private fun setupStatusBar() {
        val isDark=false
        window.statusBarColor = ContextCompat.getColor(
            this,
            if (isDark) R.color.black else R.color.white
        )
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = !isDark
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
                            R.color.primary
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


    private fun startFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_of_super, fragment)
        fragmentTransaction.addToBackStack(null) // Optional: adds the transaction to the back stack
        fragmentTransaction.commit()
    }



}