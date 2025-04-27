package com.example.orderfoodgo.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.fragment.CartFragment;
import com.example.orderfoodgo.fragment.FavoriteFragment;
import com.example.orderfoodgo.fragment.HomeFragment;
import com.example.orderfoodgo.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        openFragment(new HomeFragment());
        if (getIntent().getBooleanExtra("showProfileFragment", false)) {
            openFragment(new ProfileFragment());
            // Đặt trạng thái item "Profile" được chọn trong BottomNavigationView
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setSelectedItemId(R.id.profile);
        } else {
            // Trường hợp mặc định
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setSelectedItemId(R.id.home);
        }
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                openFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.profile) {
                openFragment(new ProfileFragment());
                return true;
            } else if (itemId == R.id.cart) {
                openFragment(new CartFragment());
                return true;
            } else if (itemId == R.id.favorites) {
                openFragment(new FavoriteFragment());
                return true;
            }
            return false;
        });
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}