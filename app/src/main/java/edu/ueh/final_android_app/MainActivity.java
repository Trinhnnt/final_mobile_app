package edu.ueh.final_android_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.ueh.final_android_app.databinding.ActivityMainBinding;
import edu.ueh.final_android_app.util.CommonUtil;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            if (!prefs.getBoolean("is_logged_in", false)) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return false;
            }

            int id = menuItem.getItemId();
            if (id == R.id.home) replaceFragment(new HomeFragment());
            else if (id == R.id.account) replaceFragment(new AccountFragment());
            else if (id == R.id.create) replaceFragment(new CreateFragment());
            return true;
        });
        if (!isLoggedIn) {
            replaceFragment(new LoginFragment());
            binding.bottomNavigationView.setVisibility(View.GONE); // hide navigation
        } else {
            replaceFragment(new HomeFragment());
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    public void onLoginSuccess() {
        prefs.edit().putBoolean("is_logged_in", true).apply();
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        replaceFragment(new HomeFragment());
    }

    public void onLogoutSuccess() {
        CommonUtil.currentUser = null;
        prefs.edit().putBoolean("is_logged_in", false).apply();
        binding.bottomNavigationView.setVisibility(View.GONE);
        replaceFragment(new LoginFragment());
    }

    public void goToSignUp() {
        replaceFragment(new SignUpFragment());
    }

    public void goToSignIn() {
        replaceFragment(new LoginFragment());
    }

    public void goToHomepage() {
        replaceFragment(new HomeFragment());
    }
}