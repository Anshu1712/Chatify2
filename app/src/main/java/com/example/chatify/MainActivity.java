package com.example.chatify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatify.Status.imageActivity;
import com.example.chatify.adapters.MyAdapter;
import com.example.chatify.contacts.AddContact;
import com.example.chatify.databinding.ActivityMainBinding;
import com.example.chatify.fragment.Fragment_Status;
import com.example.chatify.fragment.chat;
import com.example.chatify.setting.SettingsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;

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

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_GALLERY = 2;
    private Uri imageUri; // Added

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
                openCameraBs();
            }
        });
    }

    private void openCameraBs() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.camera_bs, null);
        bottomSheetDialog.setContentView(view);

        ImageView camera = view.findViewById(R.id.camera1);
        LinearLayout galleryLayout = view.findViewById(R.id.gallery1);

        camera.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openCamera();
        });

        galleryLayout.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openGallery();
        });

        bottomSheetDialog.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                imageUri = data.getData();
                openImageActivity(imageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    Intent intent = new Intent(MainActivity.this, imageActivity.class);
                    intent.putExtra("bitmap", bitmap); // Make sure imageActivity can handle Bitmap
                    startActivity(intent);
                }
            }
        }
    }

    private void openImageActivity(Uri uri) {
        Intent intent = new Intent(MainActivity.this, imageActivity.class);
        intent.putExtra("u", uri.toString());
        startActivity(intent);
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

    void initializeZego() {
        long appID = 1504216421;   // yourAppID
        String appSign = "394fa9d76da7090689c6253c135014074cd32ccd5c3ea9a82fe4df9268e3aa9b";
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userName = userID;

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(getApplication(), appID, appSign, userID, userName,callInvitationConfig);
    }
}
