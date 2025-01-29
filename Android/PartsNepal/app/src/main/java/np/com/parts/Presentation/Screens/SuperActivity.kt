package np.com.parts.Presentation.Screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.Toast
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
import np.com.parts.Domain.ViewModels.CartViewModel
import timber.log.Timber
import androidx.activity.viewModels
import androidx.navigation.ui.setupWithNavController
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import np.com.parts.API.Repository.AuthRepository
import np.com.parts.API.TokenManager
import np.com.parts.Presentation.Screens.Fragments.StartingScreens.VerifyEmailFragment
import np.com.parts.Presentation.ui.statusbar.StatusBarColors
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
private lateinit var navController: NavController
private lateinit var binding: ActivitySuperBinding
@AndroidEntryPoint
class SuperActivity : AppCompatActivity() {
    private val cartViewModel: CartViewModel by viewModels()
    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var authRepository: AuthRepository

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

        // Initialize NavController using the correct ID
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_of_super) as NavHostFragment
        navController = navHostFragment.navController

// Set up SmoothBottomBar
        setupBottomNavigation()
        observeCartBadge()
        setupMisc()
        setupStatusBar()
        setupUI()
    }

    private fun setupMisc() {
        if (tokenManager.hasToken()){
            lifecycleScope.launch{
                val accountStatus = authRepository.getAccountStatus()
                if (accountStatus.getOrDefault("ACTIVE")=="PENDING_VERIFICATION"){
                    startFragment(VerifyEmailFragment())
                }

                if (accountStatus.getOrDefault("ACTIVE")== "SUSPENDED"){
                    Toast.makeText(this@SuperActivity, "Your Account has been suspended, please contact support", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupUI() {

        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            this,
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional

                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()

    }


    private fun setupStatusBar() {


        val color = ContextCompat.getColor(this, R.color.background)
        StatusBarColors.setStatusBarColor(this, color)
        StatusBarColors.setDarkStatusBarIcons(this)

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