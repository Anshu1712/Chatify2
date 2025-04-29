package com.example.chatify;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatify.adapters.MyAdapter;
import com.example.chatify.contacts.AddContact;
import com.example.chatify.databinding.ActivityMainBinding;
import com.example.chatify.fragment.Fragment_Status;
import com.example.chatify.fragment.chat;
import com.example.chatify.setting.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    public static FloatingActionButton floatingActionButton;
    public static String nameAdmin;
    public FirebaseDatabase database;
    public FirebaseAuth auth;
    private ActivityMainBinding binding;
    private ViewPager2 viewPager;
    private Toolbar toolbar;
    private MyAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        floatingActionButton = binding.addContactnew;
        tabLayout = binding.tabLayout;
        viewPager = binding.viewPager;


        // Set up the adapter for ViewPager
        adapter = new MyAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new chat());
        adapter.addFragment(new Fragment_Status());

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(adapter);

        // TabLayoutMediator binds TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.baseline_chat_24);
                    tab.setText("Chat");
                    break;
                case 1:
                    tab.setIcon(R.drawable.baseline_data_usage_24);
                    tab.setText("Status");
                    break;
            }
        }).attach();

        // Check for the "goToStatus" flag in the intent (from imageActivity)
        if (getIntent().getBooleanExtra("goToStatus", false)) {
            viewPager.setCurrentItem(1); // Switch to "Status" tab
        }
        // Update FAB behavior based on the selected tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateFloatingActionButtonIcon(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search1);
        searchItem.getIcon().setTint(getResources().getColor(R.color.white));
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateFloatingActionButtonIcon(int position) {
        switch (position) {
            case 0: // Chat
                floatingActionButton.setImageResource(R.drawable.baseline_add_24);
                playFabPopupAnimation();
                break;

            case 1: // Status
                floatingActionButton.setImageResource(R.drawable.baseline_camera_alt_24);
                playFabPopupAnimation();
                break;

            default:
                floatingActionButton.hide();
                break;
        }
        setFabClickAction(position);
    }

    private void setFabClickAction(int index) {
        floatingActionButton.setOnClickListener(v -> {
            v.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start())
                    .start();

            if (index == 0) {
                startActivity(new Intent(MainActivity.this, AddContact.class));
            } else if (index == 1) {
                Toast.makeText(getApplicationContext(), "Camera clicked", Toast.LENGTH_SHORT).show();
                // TODO: startActivity(new Intent(MainActivity.this, CreateStatusActivity.class));
            }
        });
    }

    private void playFabPopupAnimation() {
        floatingActionButton.setScaleX(0f);
        floatingActionButton.setScaleY(0f);
        floatingActionButton.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
    }
}
