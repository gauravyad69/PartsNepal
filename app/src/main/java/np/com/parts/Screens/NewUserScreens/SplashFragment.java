package np.com.parts.Screens.NewUserScreens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import np.com.parts.R;
import np.com.parts.databinding.FragmentSplashBinding;


public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Start the next fragment after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startNextFragmentAndClose();
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }

    private void startNextFragmentAndClose() {
        // Replace with your actual navigation logic
        NavHostFragment.findNavController(SplashFragment.this)
                .navigate(R.id.action_splashFragment_to_welcomeFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}