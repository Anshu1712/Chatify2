package com.example.chatify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatify.adapters.MyAdapter;
import com.example.chatify.contacts.AddContact;
import com.example.chatify.contacts.NewGroup;
import com.example.chatify.databinding.ActivityMainBinding;
import com.example.chatify.fragment.Call;
import com.example.chatify.fragment.Fragment_Status;
import com.example.chatify.fragment.chat;
import com.example.chatify.setting.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPager2 viewPager;
    private Toolbar toolbar;
    private MyAdapter adapter;
    private FloatingActionButton floatingActionButton;
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

        adapter = new MyAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new chat());
        adapter.addFragment(new Fragment_Status());
        adapter.addFragment(new Call());

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(adapter);

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
                case 2:
                    tab.setIcon(R.drawable.baseline_call_24);
                    tab.setText("Call");
                    break;
            }
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateFloatingActionButtonIcon(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  // ✅ Fixed Method Name
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search1);
        searchItem.getIcon().setTint(getResources().getColor(R.color.white)); // Change 'white' to any color in your colors.xml
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
        // Handling item selection in the app bar menu
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Handling the settings menu item click
            Log.d("MainActivity", "Settings item clicked");
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (id == R.id.action_new_group) {
            // Handling the new group menu item click
            Log.d("MainActivity", "New Group item clicked");
            Intent newGroupIntent = new Intent(MainActivity.this, NewGroup.class);
            startActivity(newGroupIntent);
            return true;
        } else {
            return super.onOptionsItemSelected(item); // Default action if the item is not recognized
        }
    }

    private void updateFloatingActionButtonIcon(int position) {  // ✅ Add FAB Behavior
        switch (position) {
            case 0: // Chat
                floatingActionButton.setImageResource(R.drawable.baseline_add_24);
                binding.addContactnew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, AddContact.class);
                        startActivity(intent);
                    }
                });
                break;
            case 1: // Status
                floatingActionButton.setImageResource(R.drawable.baseline_camera_alt_24);
                floatingActionButton.show();
                break;
            case 2: // Call
                floatingActionButton.setImageResource(R.drawable.baseline_call_24);
                floatingActionButton.show();
                break;
            default:
                floatingActionButton.hide();
                break;
        }
    }
}
